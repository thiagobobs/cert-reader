package com.reader.cert;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import javax.imageio.ImageIO;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	private HttpServer httpServer;

	public static void main(String[] args) {
		BasicConfigurator.configure();
		new Main().iniciaServidorWeb();
	}

	public void iniciaServidorWeb() {
		try {
			this.httpServer = HttpServer.create(new InetSocketAddress(8400), 0);
			this.criaContextos(this.httpServer);
		} catch (Exception ex) {
			logger.error("Erro ao iniciar servidor web", ex);
		}
	}

	public void paraServidorWeb() {
		this.httpServer.stop(0);
	}

	private void criaContextos(HttpServer httpServer) {
		httpServer.createContext("/", new HttpHandler() {

			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				byte[] result = ("{\"certificado\": \"" + recuperaInfoCertificado() + "\"}").getBytes();

				httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
				httpExchange.sendResponseHeaders(200, result.length);

				try (OutputStream os = httpExchange.getResponseBody()) {
					os.write(result);
					os.flush();
				} finally {
					httpExchange.close();
				}
			}

		});
		httpServer.setExecutor(null);
		httpServer.start();
	}

	private String recuperaInfoCertificado() {
//		String filePath = "/home/thiago/Development/certificates/cnj/cnj.p12";
//		String password = "Hlprm4ckAB0ZF";

		String result = "";

		try {
			Class<?> classe = Class.forName("sun.security.pkcs11.SunPKCS11");
			Constructor<?> constructor = classe.getConstructor(InputStream.class);
			Provider provider = (Provider) constructor.newInstance(this.getConfigFile("Test123", "/usr/lib/libeToken.so"));

			if (provider != null) {
				Security.addProvider(provider);

				KeyStore.ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(new CallbackHandler() {

					@Override
					public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
						for (Callback callback : callbacks) {
							if (callback instanceof PasswordCallback) {
								PasswordCallback passwordCallback = (PasswordCallback) callback;

								JLabel label1 = new JLabel("Tipo: ");
								JLabel label2 = new JLabel("Descrição: ");
								JLabel label3 = new JLabel("");
								JLabel label4 = new JLabel("Insira o PIN:");
								JPasswordField passField = new JPasswordField();

								JOptionPane jop = new JOptionPane(
										new Object[] { label1, label2, label3, label4, passField },
										JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

								JDialog dialog = jop.createDialog("Insira o PIN:");
								dialog.addComponentListener(new ComponentAdapter() {
									@Override
									public void componentShown(ComponentEvent e) {
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												passField.requestFocusInWindow();
											}
										});
									}
								});

								passField.setText("");

								try {
									dialog.setIconImage(ImageIO.read(getClass().getResourceAsStream("/icons/key.png")));
								} catch (Exception e) {

								}
								dialog.setAlwaysOnTop(true);
								dialog.setVisible(true);

								int result = (Integer) jop.getValue();
								dialog.dispose();
								if (result == JOptionPane.OK_OPTION) {
									passwordCallback.setPassword(passField.getPassword());
								} else {
									passwordCallback.clearPassword();
									throw new IOException(new Exception());
								}
							}
						}
					}

				});	
				KeyStore.Builder keyStoreBuilder = KeyStore.Builder.newInstance("PKCS11", provider, protectionParameter);

				ObjectMapper mapper = new ObjectMapper();
				result = mapper.writeValueAsString(new Response(keyStoreBuilder.getKeyStore()));
			}

//			var keystore = KeyStore.getInstance(KeyStore.getDefaultType());
//			try (InputStream in = new FileInputStream(filePath)) {
//				keystore.load(in, password.toCharArray());
//			}
//
//			result = keystore.getKey(keystore.aliases().nextElement(), password.toCharArray()).toString();
		} catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}

		return result;
	}

	private ByteArrayInputStream getConfigFile(String providerName, String pathToPKCS11) {
		StringBuilder content = new StringBuilder("name=").append(providerName).append("\n").append("library=")
				.append(pathToPKCS11);

		return new ByteArrayInputStream(content.toString().getBytes());
	}

}
