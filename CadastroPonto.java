package entrega1;

import javax.swing.*;

import org.json.JSONObject;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;



public class CadastroPonto extends JFrame {
    private JTextField nomeField;
    private JTextField obsField;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private String token;

    public CadastroPonto(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Cadastro de Ponto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel nomeLabel = new JLabel("Nome:");
        nomeField = new JTextField(20);

        JLabel obsLabel = new JLabel("Observacoes:");
        obsField = new JTextField(20);


        constraints.gridy = 0;
        panel.add(nomeLabel, constraints);

        constraints.gridy = 1;
        panel.add(obsLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(nomeField, constraints);

        constraints.gridy = 1;
        panel.add(obsField, constraints);

        JButton cadastrarButton = new JButton("Cadastrar");
        cadastrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cadastrarPonto();
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

    private void cadastrarPonto() {
        String name = nomeField.getText();
        String obs = obsField.getText();


        if (name.isEmpty() || obs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios.");
            return;
        }

        JSONObject mensagem = new JSONObject();
        mensagem.put("action", "cadastro-ponto");

        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("token", token);
        data.put("obs", obs);
        mensagem.put("data", data);

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);            
            out.println(mensagem.toString());
            System.out.println("TelaCadastroPonto-> Enviado para o servidor: "+mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            if (resposta != null) {
	            System.out.println("TelaCadastroPonto<-Recebida do servidor: "+ resposta);
	            JSONObject respostaJSON = new JSONObject(resposta);           
                String action = respostaJSON.optString("action");
               	Boolean error = respostaJSON.optBoolean("error");
                String message = respostaJSON.optString("message");
                if (error == null || error.toString().isEmpty()) {
	                JOptionPane.showMessageDialog(this, "Campo 'error' não enviado pelo Servidor ou nulo");
	                return; 
	            }
	            if (action == null || action.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "Campo 'action' não enviado pelo Servidor ou nulo");
	            }
	            if (message == null || message.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "Campo 'message' não enviado pelo Servidor ou nulo");
	            }

             if (!error) {
                JOptionPane.showMessageDialog(this, message);
                setVisible(false);
                telaPrincipal.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, message);
            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void voltarTelaPrincipal() {
    	telaPrincipal.atualizarToken(token);
        telaPrincipal.setVisible(true);
        dispose();
    }
    public void atualizarSocket(Socket novoSocket) {
        this.socket = novoSocket;
    }  
    public void atualizarToken(String novoToken) {
        this.token = novoToken;
    }  

}

