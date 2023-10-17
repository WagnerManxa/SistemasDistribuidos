package entrega1;

import javax.swing.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

        JButton listarUsuariosButton = new JButton("Listar Usuários");
        listarUsuariosButton.setEnabled(false);
        listarUsuariosButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarUsuarios();
            }
        });
        panel.add(listarUsuariosButton);

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
    
    public void atualizarSocket(Socket novoSocket) {
        this.socket = novoSocket;
    }  

    private void abrirCadastroUsuario() {
        CadastroUsuario cadastroUsuario = new CadastroUsuario(socket, this, token);
        cadastroUsuario.setVisible(true);
        this.dispose();
    }

    private void listarUsuarios() {
    	ListarUsuario listarUsuario = new ListarUsuario(socket,null, token);
    	listarUsuario.setVisible(true);
        //JOptionPane.showMessageDialog(this, "Operação de Listar Usuários");
        // Lógica para a operação de listar usuários
    }

    private void abrirEditarUsuario() {
        EditarUsuario editarUsuario = new EditarUsuario(socket, this, token);
        editarUsuario.setVisible(true);
    }

    private void excluirUsuario() {
        JOptionPane.showMessageDialog(this, "Operação de Excluir Usuário");
        // Lógica para a operação de excluir usuário
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

        try {
            JSONObject mensagem = new JSONObject();
            mensagem.put("action", "logout");
            JSONObject data = new JSONObject();
            data.put("token", token);
            mensagem.put("data", data);

            // Enviando a mensagem JSON para o servidor
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem.toString());
            System.out.println("TelaPrincipal->Enviada ao servidor: "+ mensagem);

            // Aguardando a resposta do servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            System.out.println("TelaPrincipal<-Recebida do servidor: "+ resposta);
            // Analisando a resposta do servidor
            JSONObject respostaJSON = new JSONObject(resposta);
            boolean error = respostaJSON.optBoolean("error");
            String message = respostaJSON.optString("message");

            if (!error) {
                JOptionPane.showMessageDialog(this, message);
                voltarTelaLogin();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao efetuar logout: " + message);
            }
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao efetuar logout.");
        }
    }
}
