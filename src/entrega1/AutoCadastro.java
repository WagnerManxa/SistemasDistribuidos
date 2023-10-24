package entrega1;
import javax.swing.*;

import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AutoCadastro extends JFrame {
    private JTextField nomeField;
    private JTextField emailField;
    private JPasswordField senhaField;
    private JTextField serverIpField;
    private JTextField serverPortField;
    private Socket socket;
    private ClienteLogin clienteLogin;

    public AutoCadastro(Socket socket, ClienteLogin clienteLogin) {
        this.socket = socket;
        this.clienteLogin = clienteLogin;

        setTitle("Autocadastro de Usuário Comum");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(250, 450);

        JPanel panel = new JPanel();

        JPanel autocadastroPanel = new JPanel();
        autocadastroPanel.setLayout(new GridLayout(14, 1));

        JLabel nomeLabel = new JLabel("Nome:");
        nomeField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);

        JLabel senhaLabel = new JLabel("Senha:");
        senhaField = new JPasswordField(20);

        JLabel serverIpLabel = new JLabel("IP:");
        serverIpField = new JTextField();

        JLabel serverPortLabel = new JLabel("Porta:");
        serverPortField = new JTextField();

        autocadastroPanel.add(nomeLabel);
        autocadastroPanel.add(nomeField);
        autocadastroPanel.add(emailLabel);
        autocadastroPanel.add(emailField);
        autocadastroPanel.add(senhaLabel);
        autocadastroPanel.add(senhaField);

        autocadastroPanel.add(serverIpLabel);
        autocadastroPanel.add(serverIpField);
        autocadastroPanel.add(serverPortLabel);
        autocadastroPanel.add(serverPortField);

        autocadastroPanel.add(new JPanel());

        JButton cadastrarButton = new JButton("Cadastrar");
        cadastrarButton.setPreferredSize(new Dimension(120, 30));
        cadastrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cadastrarUsuario();
            }
        });
        

        JButton backButton = new JButton("Voltar");
        backButton.setPreferredSize(new Dimension(80, 30));
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                voltarTelaLogin();
            }
        });
        
        autocadastroPanel.add(cadastrarButton);
      
        autocadastroPanel.add(backButton);
        
        panel.add(autocadastroPanel, BorderLayout.PAGE_START);

        add(panel);
        setLocationRelativeTo(null);
    }
    private void cadastrarUsuario() {
        String nome = nomeField.getText();
        String email = emailField.getText();
        String senha = new String(senhaField.getPassword());
        String serverIp = serverIpField.getText();
        String serverPortText = serverPortField.getText();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || serverIp.isEmpty() || serverPortText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios.");
            return;
        }

        if (senha.length() < 6) {
            JOptionPane.showMessageDialog(this, "A senha deve ter no minimo 6 caracteres.");
            return;
        }

        String senhaMD5 = hashMD5(senha).toUpperCase();

        JSONObject mensagem = new JSONObject();
        mensagem.put("action", "autocadastro-usuario");

        JSONObject data = new JSONObject();
        data.put("name", nome);
        data.put("email", email);
        data.put("password", senhaMD5);

        mensagem.put("data", data);

        try {
            // Abrir o socket
            Socket socket = new Socket(serverIp, Integer.parseInt(serverPortText));

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem.toString());
            System.out.println("AutoCadastro->Enviada para o servidor: " + mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();

            if (resposta == null) {
            	System.out.println("AutoCadastro<-Recebida do servidor: null");
                socket.close();
            	return;
            }else {
            	System.out.println("AutoCadastro<-Recebida do servidor: "+resposta);
                JSONObject respostaJSON = new JSONObject(resposta);
                boolean error = respostaJSON.optBoolean("error");
                String message = respostaJSON.optString("message", "");

                if (!error) {
                    JOptionPane.showMessageDialog(this, message);
                    setVisible(false);
                    clienteLogin.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, message);
                }
            }
            
       
            socket.close();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "A porta do servidor deve ser um número válido.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Falha na conexão com o servidor.\nVerifique o Ip e porta");
        }
    }

    private void voltarTelaLogin() {
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