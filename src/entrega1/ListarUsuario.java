package entrega1;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ListarUsuario extends JFrame {
    private JTextArea listaUsuariosTextArea;
    private Socket socket;
    private JButton listarButton;
    private JButton voltarButton; 
    private TelaPrincipal telaPrincipal;
    private static String token;

    public ListarUsuario(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Lista de Usuários");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new BorderLayout());

        listaUsuariosTextArea = new JTextArea();
        listaUsuariosTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(listaUsuariosTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        listarButton = new JButton("Listar Usuários");
        listarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
                listarUsuarios();
            }
        });

        // Botão Voltar
        voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                voltarTelaAnterior();
            }
        });

        // Adiciona os botões ao painel de botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(listarButton);
        buttonPanel.add(voltarButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setLocationRelativeTo(null);
    }

    private void listarUsuarios() {
        try {
            JSONObject mensagem = new JSONObject();
            mensagem.put("action", "listar-usuarios");

            JSONObject data = new JSONObject();
            data.put("token", token); // Substitua "exemplo-de-token" pelo token real

            mensagem.put("data", data);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem.toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();

            JSONObject respostaJson = new JSONObject(resposta);
            boolean error = respostaJson.optBoolean("error", true);

            if (!error) {
                JSONObject dataJson = respostaJson.getJSONObject("data");
                JSONArray usuarios = dataJson.getJSONArray("usuarios");

                StringBuilder lista = new StringBuilder();
                lista.append("Usuários cadastrados:\n\n");

                for (int i = 0; i < usuarios.length(); i++) {
                    JSONObject usuario = usuarios.getJSONObject(i);
                    int usuarioId = usuario.getInt("id");
                    String nome = usuario.getString("nome");
                    String email = usuario.getString("email");
                    String tipo = usuario.getString("tipo");

                    lista.append("ID: ").append(usuarioId).append("\n");
                    lista.append("Nome: ").append(nome).append("\n");
                    lista.append("Email: ").append(email).append("\n");
                    lista.append("Tipo: ").append(tipo).append("\n\n");
                }

                listaUsuariosTextArea.setText(lista.toString());
            } else {
                listaUsuariosTextArea.setText("Erro ao listar usuários.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Método para voltar para a tela anterior
    private void voltarTelaAnterior() {
        setVisible(false);
        telaPrincipal.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Socket socket = new Socket();
                TelaPrincipal telaPrincipal = new TelaPrincipal(socket, null, null);
                ListarUsuario listaUsuarios = new ListarUsuario(socket, telaPrincipal, token);
                listaUsuarios.setVisible(true);
            }
        });
    }
}