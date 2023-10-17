package entrega1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

public class EditarUsuario extends JFrame {
    private JTextField idUsuarioField;
    private JTextField nomeFieldEditar;
    private JTextField emailFieldEditar;
    private JComboBox<String> tipoComboBoxEditar;
    private JPasswordField senhaFieldEditar;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private PrintWriter out;
    private BufferedReader in;
    private static String token;

    public EditarUsuario(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Editar Usuário");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel idUsuarioLabel = new JLabel("ID do Usuário:");
        idUsuarioField = new JTextField();
        idUsuarioField.setPreferredSize(new Dimension(150, 25));

        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarUsuario();
            }
        });

        // Adicione os campos para edição diretamente abaixo do botão "Buscar"
        JLabel nomeLabelEditar = new JLabel("Nome:");
        nomeFieldEditar = new JTextField(20);

        JLabel emailLabelEditar = new JLabel("Email:");
        emailFieldEditar = new JTextField(20);

        JLabel tipoLabelEditar = new JLabel("Tipo de Usuário:");
        String[] tiposEditar = {"admin", "user"};
        tipoComboBoxEditar = new JComboBox<>(tiposEditar);
        tipoComboBoxEditar.setSelectedIndex(1);

        JLabel senhaLabelEditar = new JLabel("Senha:");
        senhaFieldEditar = new JPasswordField(20);

        constraints.gridy = 1;
        panel.add(idUsuarioLabel, constraints);

        constraints.gridx = 1;
        panel.add(idUsuarioField, constraints);

        constraints.gridy = 2;
        constraints.gridx = 0;
        panel.add(nomeLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(nomeFieldEditar, constraints);

        constraints.gridy = 3;
        constraints.gridx = 0;
        panel.add(emailLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(emailFieldEditar, constraints);

        constraints.gridy = 4;
        constraints.gridx = 0;
        panel.add(tipoLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(tipoComboBoxEditar, constraints);

        constraints.gridy = 5;
        constraints.gridx = 0;
        panel.add(senhaLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(senhaFieldEditar, constraints);

        constraints.gridy = 6;
        constraints.gridx = 0;
        panel.add(buscarButton, constraints);

        JButton salvarButton = new JButton("Salvar");
        //salvarButton.setEnabled(false);
        salvarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salvarAlteracoes();
            }
        });

        constraints.gridy = 7;
        panel.add(salvarButton, constraints);

        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                voltarTelaPrincipal();
            }
        });

        constraints.gridy = 8;
        constraints.gridx = 0;
        panel.add(voltarButton, constraints);

        getContentPane().add(panel);

        pack();
        setLocationRelativeTo(null);

        // Inicializar o PrintWriter e o BufferedReader aqui
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buscarUsuario() {
        String idUsuarioStr = idUsuarioField.getText();
        if (idUsuarioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do usuário.");
            return;
        }

        int idUsuario;
        try {
            idUsuario = Integer.parseInt(idUsuarioStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID do usuário deve ser um número inteiro.");
            return;
        }

        JSONObject mensagem = new JSONObject();
        mensagem.put("action", "pedido-edicao-usuario");

        JSONObject data = new JSONObject();
        data.put("token", "exemplo-de-token");
        data.put("usuario_id", idUsuario);

        mensagem.put("data", data);

        out.println(mensagem.toString());

        try {
            // Agora, aguarda a resposta do servidor
            String resposta = in.readLine();

            // Exiba a resposta no JTextArea (removido)

            // Adicione lógica para preencher os campos de edição com os dados do usuário
            JSONObject respostaJson = new JSONObject(resposta);
            if (!respostaJson.getBoolean("error")) {
                JSONObject usuarioData = respostaJson.getJSONObject("data");
                nomeFieldEditar.setText(usuarioData.getString("nome"));
                emailFieldEditar.setText(usuarioData.getString("email"));
                tipoComboBoxEditar.setSelectedItem(usuarioData.getString("tipo"));
                // Se necessário, trate a senha de maneira diferente, como não exibí-la aqui
            }

        } catch (SocketException se) {
            // Trate a exceção específica para conexão fechada
            JOptionPane.showMessageDialog(this, "A conexão foi fechada pelo servidor. Tente novamente.");
            voltarTelaPrincipal();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao receber resposta do servidor.");
        }
    }

    private void salvarAlteracoes() {
        // Adicione a lógica para enviar as alterações para o servidor e tratar a resposta
        // Exemplo:
        String idUsuarioStr = idUsuarioField.getText();
        if (idUsuarioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do usuário.");
            return;
        }

        int idUsuario;
        try {
            idUsuario = Integer.parseInt(idUsuarioStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID do usuário deve ser um número inteiro.");
            return;
        }

        String nome = nomeFieldEditar.getText();
        String email = emailFieldEditar.getText();
        String tipo = (String) tipoComboBoxEditar.getSelectedItem();
        String senha = new String(senhaFieldEditar.getPassword());
        String senhaMD5 = DigestUtils.md5Hex(senha);
        if (senha.length() < 6) {
            JOptionPane.showMessageDialog(this, "A senha deve ter no mínimo 6 caracteres.");
            return;
        }

        JSONObject mensagem = new JSONObject();
        mensagem.put("action", "salvar-alteracoes-usuario");

        JSONObject data = new JSONObject();
        data.put("token", "exemplo-de-token");
        data.put("usuario_id", idUsuario);
        data.put("nome", nome);
        data.put("email", email);
        data.put("tipo", tipo);
        data.put("senha", senhaMD5);
        mensagem.put("data", data);

        out.println(mensagem.toString());

        try {
        	
            // Agora, aguarda a resposta do servidor
            String resposta = in.readLine();

            // Exiba a resposta ou trate conforme necessário
            JOptionPane.showMessageDialog(this, "Resposta do servidor: " + resposta);

        } catch (SocketException se) {
            // Trate a exceção específica para conexão fechada
            JOptionPane.showMessageDialog(this, "A conexão foi fechada pelo servidor. Tente novamente.");
            voltarTelaPrincipal();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao receber resposta do servidor.");
        }
    }

    private void voltarTelaPrincipal() {
        setVisible(false);
        telaPrincipal.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Substitua por código real para obter o socket e a tela principal
                Socket socket = new Socket();
                TelaPrincipal telaPrincipal = new TelaPrincipal(socket, token, null);
                EditarUsuario editarUsuario = new EditarUsuario(socket, telaPrincipal, token);
                editarUsuario.setVisible(true);
            }
        });
    }
}