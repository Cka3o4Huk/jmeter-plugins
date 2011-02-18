package kg.apc.jmeter.samplers;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * @author undera
 */
public class HTTPRawSampler extends AbstractSampler {

    static final String HOSTNAME = "hostname";
    static final String PORT = "port";
    static String BODY = "body";
    // 
    private static final Logger log = LoggingManager.getLoggerForClass();
    private ByteBuffer recvBuf = ByteBuffer.allocateDirect(1024 * 1); // TODO: make benchmarks to know how it impacts sampler performance
    private ByteBuffer sendBuf;
    private SocketAddress addr;
    //private SocketChannel sock;

    public HTTPRawSampler() {
        super();
    }

    public final String getHostName() {
        return getPropertyAsString(HOSTNAME);
    }

    void setHostName(String text) {
        addr = null; // reset cached addr and SendBuf
        setProperty(HOSTNAME, text);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
    }

    public void setPort(String value) {
        addr = null; // reset cached addr and SendBuf
        setProperty(PORT, value);
    }

    public String getRawRequest() {
        return getPropertyAsString(HTTPRawSampler.BODY);
    }

    public void setRawRequest(String value) {
        addr = null; // reset cached addr and SendBuf
        setProperty(BODY, value);
    }

    public SampleResult sample(Entry e) {
        if (addr == null) {
            log.debug("Create cached values");
            int port;
            try {
                port = Integer.parseInt(getPort());
            } catch (NumberFormatException ex) {
                log.warn("Wrong port number: " + getPort() + ", defaulting to 80", ex);
                port=80;
            }
            //log.info("B "+getRawRequest().length());
            sendBuf = ByteBuffer.wrap(getRawRequest().getBytes());
            //log.info("A "+sendBuf.array().length);
            addr = new InetSocketAddress(getHostName(), port);
        }

        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(getRawRequest());
        res.sampleStart();
        res.setSuccessful(true);
        try {
            res.setResponseData(processIO(res));
        } catch (Exception ex) {
            log.error(getHostName(), ex);
            res.sampleEnd();
            res.setSuccessful(false);
        }

        return res;
    }

    private byte[] processIO(SampleResult res) throws Exception {
        //log.info("Begin IO");
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        //log.info("Open sock");
        SocketChannel sock = SocketChannel.open(addr);
        //sock.connect(addr);
        //sock.connect(addr);

        //log.info("send");
        sendBuf.rewind();
        sock.write(sendBuf);
        int cnt = 0;
        recvBuf.clear();

        while ((cnt = sock.read(recvBuf)) != -1) {
            //log.info("Read "+cnt);
            recvBuf.flip();
            byte[] bytes = new byte[cnt];
            recvBuf.get(bytes);
            response.write(bytes);
            recvBuf.clear();
        }
        res.sampleEnd();

        sock.close();
        //log.info("End IO");
        return response.toByteArray();
    }
}
