package com.reader.cert;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class InputPassword extends JFrame {

	public static void main(String[] args) {
		JPanel panel = new JPanel();
		panel.add(new JLabel("Senha: "));
		panel.add(new JTextField(15));
		
		InputPassword inputPassword = new InputPassword();
		inputPassword.add(panel);
		inputPassword.setSize(300, 100);
		inputPassword.show();
	}
	
}
