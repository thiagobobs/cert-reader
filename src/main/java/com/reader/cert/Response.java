package com.reader.cert;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class Response {

	protected X509Certificate certificate;
	protected String certificateAlias;
	protected KeyStore keyStore;
	protected PrivateKey privateKey;
	protected Certificate[] certsChain;

	public Response(KeyStore keyStore) {
		super();
		this.keyStore = keyStore;

		try {
			this.certificateAlias = keyStore.getCertificateAlias(certificate);
			this.certificate = (X509Certificate) keyStore.getCertificate(this.certificateAlias);
			this.privateKey = (PrivateKey) keyStore.getKey(this.certificateAlias, null);
			this.certsChain = keyStore.getCertificateChain(this.certificateAlias);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public String getCertificateAlias() {
		return certificateAlias;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public Certificate[] getCertsChain() {
		return certsChain;
	}

}
