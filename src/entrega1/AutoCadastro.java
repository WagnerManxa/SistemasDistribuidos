package entrega1;

import javax.swing.*;

import org.json.JSONObject;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class AutoCadastro extends JFrame {
    private JTextField nomeField;
    private JTextField emailField;
    private JPasswordField senhaField;
    private Socket socket;
    private ClienteLogin clienteLogin;

    public AutoCadastro(Socket socket,  ClienteLogin clienteLogin) {
        this.socket = socket;
        this.clienteLogin = clienteLogin;

        setTitle("Autocadastro de Usuário Comum");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel nomeLabel = new JLabel("Nome:");
        nomeField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);
        

        JLabel senhaLabel = new JLabel("Senha:");
        senhaField = new JPasswordField(20);

        constraints.gridy = 0;
        panel.add(nomeLabel, constraints);

        constraints.gridy = 1;
        panel.add(emailLabel, constraints);


        constraints.gridy = 3;
        panel.add(senhaLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(nomeField, constraints);

        constraints.gridy = 1;
        panel.add(emailField, constraints);

        constraints.gridy = 3;
        panel.add(senhaField, constraints);

        JButton cadastrarButton = new JButton("Cadastrar");
        cadastrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cadastrarUsuario();
            }
        });

        JButton backButton = new JButton("Voltar");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                voltarTelaPrincipal();
            }
        });

        constraints.gridy = 5;
        panel.add(backButton, constraints);

        constraints.gridy = 4;
        panel.add(cadastrarButton, constraints);

        add(panel);
        setLocationRelativeTo(null);
    }

    private void cadastrarUsuario() {
        String nome = nomeField.getText();
        String email = emailField.getText();
        String senha = new String(senhaField.getPassword());

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios.");
            return;
        }

        if (senha.length() < 6) {
            JOptionPane.showMessageDialog(this, "A senha deve ter no mínimo 6 caracteres.");
            return;
        }

        String senhaMD5 = hashMD5(senha)/*.toUpperCase()*/;

        JSONObject mensagem = new JSONObject();
        mensagem.put("action", "autocadastro-usuario");

        JSONObject data = new JSONObject();
        data.put("nome", nome);
        data.put("email", email);
        data.put("senha", senhaMD5);

        mensagem.put("data", data);

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);            
            out.println(mensagem.toString());
            System.out.println("AutoCadastro-> Enviado para o servidor: "+mensagem);

            // Agora, aguarda a resposta do servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            if (resposta != null) {
	            System.out.println("TelaCadastro<-Recebida do servidor: "+ resposta);
	            JSONObject respostaJSON = new JSONObject(resposta);           
                String action = respostaJSON.optString("action");
               	boolean error = respostaJSON.optBoolean("error");
                String message = respostaJSON.optString("message");
             if (!error) {
                JOptionPane.showMessageDialog(this, message);
                setVisible(false);
                clienteLogin.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, message);
            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void voltarTelaPrincipal() {
    	clienteLogin.setVisible(true);
        dispose();
    }

    private String hashMD5(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] senhaBytes = senha.getBytes();
            byte[] hashBytes = md.digest(senhaBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

