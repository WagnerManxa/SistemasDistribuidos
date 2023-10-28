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
import java.net.SocketException;

public class TelaPrincipal extends JFrame {
    private ClienteLogin clienteLogin;
    private Socket socket;
    private String token;
    

    public TelaPrincipal(Socket socket, String token, ClienteLogin clienteLogin) {
        this.socket = socket;
        this.clienteLogin = clienteLogin;
        this.token = token;
        setTitle("Tela Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new FlowLayout());
       
        JButton cadastrarUsuarioButton = new JButton("Cadastrar Usuário");
        cadastrarUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirCadastroUsuario();
            }
        });
        panel.add(cadastrarUsuarioButton);

        JButton informacaoUsuarioButton = new JButton("Informações de Usuários");
        informacaoUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarUsuarios();
            }
        });
        panel.add(informacaoUsuarioButton);

        JButton editarUsuarioButton = new JButton("Editar Usuário");
        editarUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirEditarUsuario();
            }
        });
        panel.add(editarUsuarioButton);

        JButton excluirUsuarioButton = new JButton("Excluir Usuário");
        excluirUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                excluirUsuario();
            }
        });
        panel.add(excluirUsuarioButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enviarLogout(token); 
            }
        });
        panel.add(logoutButton);

        add(panel);
        setLocationRelativeTo(null);
    }
    private void showConnectionLostAlert() {
        JOptionPane.showMessageDialog(this, "Conexão com o servidor perdida!");
    }
    
    public void atualizarSocket(Socket novoSocket) {
        this.socket = novoSocket;
    }  
    public void atualizarToken(String novoToken) {
        this.token = novoToken;
    }  

    private void abrirCadastroUsuario() {
        CadastroUsuario cadastroUsuario = new CadastroUsuario(socket, this,token);
        cadastroUsuario.atualizarToken(this.token);
        cadastroUsuario.setVisible(true);
        this.dispose();
    }
    
    private void listarUsuarios() {
    	InformacaoUsuario listarUsuario = new InformacaoUsuario(socket,this, this.token);
    	listarUsuario.atualizarToken(this.token);
    	listarUsuario.setVisible(true);

    }

    private void abrirEditarUsuario() {
        EditarUsuario editarUsuario = new EditarUsuario(socket, this, this.token);
        editarUsuario.atualizarToken(this.token);
        editarUsuario.setVisible(true);
    }

    private void excluirUsuario() {
        ExcluirUsuario excluirUsuario = new ExcluirUsuario(socket, this, this.token);
        excluirUsuario.atualizarToken(this.token);
        excluirUsuario.setVisible(true);
    }

    private void voltarTelaLogin() {
    	try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        clienteLogin.setVisible(true);
        dispose(); 
    }
    
    private void enviarLogout(String token) {
        if (token == null || token.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Token inválido. O logout falhou.");
            
            return;
        }

            JSONObject mensagem = new JSONObject();
            mensagem.put("action", "logout");
            JSONObject data = new JSONObject();
            data.put("token", this.token);
            mensagem.put("data", data);

            // Enviando a mensagem JSON para o servidor
            PrintWriter out;
            BufferedReader in;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(mensagem.toString());
	            System.out.println("TelaPrincipal->Enviada ao servidor: "+ mensagem);
	            
	            try {
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String resposta = in.readLine();
		            System.out.println("TelaPrincipal<-Recebida do servidor: "+ resposta);
		            JSONObject respostaJSON = new JSONObject(resposta);
		            boolean error = respostaJSON.optBoolean("error");
		            String message = respostaJSON.optString("message");
		            if (!error) {
		                JOptionPane.showMessageDialog(this, message);
		                voltarTelaLogin();
		            } else {
		                JOptionPane.showMessageDialog(this, "Erro ao efetuar logout: " + message);
		            }
		            
		            
				} catch (IOException e) {
					System.out.println("TelaPrincipal: Erro ao receber resposta do servidor "+e.getMessage());
				    showConnectionLostAlert();
				    voltarTelaLogin();
				}
			} catch (SocketException se) {
			    // Trate a exceção de conexão resetada aqui
			    System.out.println("TelaPrincipal: A conexão foi redefinida pelo servidor: " + se.getMessage());
			    showConnectionLostAlert();
			    voltarTelaLogin();
			} catch (IOException e) {
			    // Trate a exceção de IO aqui
			    System.out.println("TelaPrincipal->Erro ao tentar enviar comando de logout ao servidor: " + e.getMessage());
			    showConnectionLostAlert();
			    
			}
}
    }
