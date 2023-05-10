package co.bugg.quickplay.http;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * License CC BY-SA 4.0 Jan Novotný
 * <a href="https://blog.novoj.net/posts/2016-02-29-how-to-make-apache-httpclient-trust-lets-encrypt-certificate-authority/">Source</a>
 * <a href="https://creativecommons.org/licenses/by-sa/4.0/">License</a>
 */
public class TrustManagerDelegate implements X509TrustManager {
    private final X509TrustManager mainTrustManager;
    private final X509TrustManager fallbackTrustManager;
    public TrustManagerDelegate(X509TrustManager mainTrustManager, X509TrustManager fallbackTrustManager) {
        this.mainTrustManager = mainTrustManager;
        this.fallbackTrustManager = fallbackTrustManager;
    }
    @Override
    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String authType) throws CertificateException {
        try {
            mainTrustManager.checkClientTrusted(x509Certificates, authType);
        } catch(CertificateException ignored) {
            this.fallbackTrustManager.checkClientTrusted(x509Certificates, authType);
        }
    }
    @Override
    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String authType) throws CertificateException {
        try {
            mainTrustManager.checkServerTrusted(x509Certificates, authType);
        } catch(CertificateException ignored) {
            this.fallbackTrustManager.checkServerTrusted(x509Certificates, authType);
        }
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.fallbackTrustManager.getAcceptedIssuers();
    }
}