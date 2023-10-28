package entrega1;

import javax.swing.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClienteLogin extends JFrame {
    private JTextField serverIpField;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private String token;
   

    public ClienteLogin() {
    	
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(250, 420);

        JPanel panel = new JPanel();

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(12, 1));

        JLabel serverIpLabel = new JLabel("IP:");
        serverIpField = new JTextField();

        JLabel serverPortLabel = new JLabel("Porta:");
        serverPortField = new JTextField();
        
        JLabel usernameLabel = new JLabel("Email do Usuário:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Senha:");
        passwordField = new JPasswordField(20);
        
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(80, 30));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        JButton autocadastroButton = new JButton("Autocadastro");
        autocadastroButton.setPreferredSize(new Dimension(120, 30));
        autocadastroButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirTelaAutocadastro();
            }
        });

        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);

            
        loginPanel.add(serverIpLabel);
        loginPanel.add(serverIpField);
        loginPanel.add(serverPortLabel);
        loginPanel.add(serverPortField);
        
        loginPanel.add(new JPanel());
        
        loginPanel.add(loginButton);
        
        loginPanel.add(new JPanel());
        loginPanel.add(autocadastroButton); // Adicionei o botão de autocadastro

        panel.add(loginPanel, BorderLayout.PAGE_START);

        add(panel);
        setLocationRelativeTo(null);
    }

    private void abrirTelaAutocadastro() {
        AutoCadastro autocadastro = new AutoCadastro(socket, this);
        autocadastro.setVisible(true);
        setVisible(false);
    }



    private void login() {
        String emailUsuario = usernameField.getText();
        String senha = new String(passwordField.getPassword());
        String serverIp = serverIpField.getText();
        String serverPortText = serverPortField.getText();

        if (emailUsuario.isEmpty() || senha.isEmpty() || serverIp.isEmpty() || serverPortText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.");
            return;
        }
        if (senha.length() < 6) {
            JOptionPane.showMessageDialog(this, "A senha deve ter no minimo 6 caracteres.");
            return;
        }
        try {
            int serverPort = Integer.parseInt(serverPortText);
            String senhaMD5 = DigestUtils.md5Hex(senha).toUpperCase();

            JSONObject mensagem = new JSONObject();
            mensagem.put("action", "login");

            JSONObject data = new JSONObject();
            data.put("email", emailUsuario);
            data.put("password", senhaMD5);

            mensagem.put("data", data);

            if (socket != null && !socket.isClosed()) {
                // Se o socket estiver aberto, feche-o antes de criar um novo
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Criar um novo socket
            socket = new Socket(serverIp, serverPort);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem.toString());
            System.out.println("TelaLogin->Enviada para o servidor: " + mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
                if (resposta == null) {
                	System.out.println("TelaLogin<-Recebida do servidor: null");
                	return;
                }else {
                
                System.out.println("TelaLogin<-Recebida do servidor: "+resposta);
                //aviso resposta recebida do servidor             
                JSONObject respostaJSON = new JSONObject(resposta);
                String action = respostaJSON.optString("action", "");
                boolean error = respostaJSON.optBoolean("error");
                String message = respostaJSON.optString("message", "");
                
                if(!error) {                
                JSONObject dataResposta = respostaJSON.optJSONObject("data");
                this.token = dataResposta.optString("token","");        
                
                                	
                switch (action) {
				case "login": {

            		JOptionPane.showMessageDialog(this, message);
                    usernameField.setText("");
                    passwordField.setText("");
                    serverIpField.setText("");
                    serverPortField.setText("");
                    
                    if (telaPrincipal == null) {
                        telaPrincipal = new TelaPrincipal(socket, this.token, this);
                    }
                    telaPrincipal.atualizarSocket(socket);
                    telaPrincipal.atualizarToken(token);
                    telaPrincipal.setVisible(true);
                    setVisible(false);
                }
				break;
				
				}
            }else {
                JOptionPane.showMessageDialog(this, message);
                    if (socket != null && !socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "A porta do servidor deve ser um número válido.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Falha na conexão com o servidor.\nVerifique o Ip e porta");
            }
            }
				
        




    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ClienteLogin clienteLogin = new ClienteLogin();
                clienteLogin.setVisible(true);
            }
        });
    }
}