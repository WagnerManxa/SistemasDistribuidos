package entrega1;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.json.JSONObject;

public class ExcluirSegmento extends JFrame {
    private JTextField idSegmentoField;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private String token;

    public ExcluirSegmento(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Excluir Segmento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        
        JLabel paginaExcluirLabel = new JLabel("Informe os dados do segmento para exclusão:");
        
        JLabel idSegmentoLabel = new JLabel("ID do Segmento:");
        idSegmentoField = new JTextField();
        idSegmentoField.setPreferredSize(new Dimension(150, 25));


        JButton excluirButton = new JButton("Excluir");
        excluirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              excluirSegmento();
            }
        });

        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              voltarTelaPrincipal();
            }
        });

        JButton cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               limparCampos();
            }
        });
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(paginaExcluirLabel, constraints);


        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(idSegmentoLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(idSegmentoField, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;        
        panel.add(excluirButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;        
        panel.add(voltarButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(cancelarButton, constraints);

        getContentPane().add(panel);

        pack();
        setLocationRelativeTo(null);

    }
    private void excluirSegmento() {
    	String idSegmentoStr = idSegmentoField.getText();
        if (idSegmentoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do segmento.");
            return;
        }

        Integer idSegmento;
        try {
            idSegmento = Integer.parseInt(idSegmentoStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID do segmento deve ser um número inteiro.");
            return;
        }
    	JSONObject mensagem = new JSONObject();
        mensagem.put("action", "excluir-segmento");
        JSONObject data = new JSONObject();
        data.put("token", token);
        data.put("segmento_id", idSegmento);
        mensagem.put("data", data);
        
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);        
            out.println(mensagem.toString());
            System.out.println("ExcluirSegmento-> Enviado para o servidor: "+mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            System.out.println("ExcluirSegmento<- Recebida do servidor: "+resposta);
            
            JSONObject respostaJson = new JSONObject(resposta);
            String action = respostaJson.optString("action");
           	Boolean error = respostaJson.optBoolean("error");
            String message = respostaJson.optString("message");
            if (error == null || error.toString().isEmpty()) {
                JOptionPane.showMessageDialog(this, "'error' não enviado pelo Servidor ou nulo");
                return; 
            }
            if (action == null || action.isEmpty()) {
                JOptionPane.showMessageDialog(this, "'action' não enviado pelo Servidor ou nulo");
            }
            if (message == null || message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "'message' não enviado pelo Servidor ou nulo");
            }
 
            if (!error) {
                JOptionPane.showMessageDialog(this, message);
                limparCampos();
            }else {
            	 JOptionPane.showMessageDialog(this, message);
            }

        } catch (SocketException se) {
            JOptionPane.showMessageDialog(this, "A conexão foi encerrada pelo servidor. Tente novamente.");
            voltarTelaPrincipal();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao receber resposta do servidor.");
        }
    }
    
    
    private void limparCampos() {
        idSegmentoField.setText("");

    }
    
    
    private void voltarTelaPrincipal() {
    	limparCampos();
        setVisible(false);
        telaPrincipal.setVisible(true);
    }
    

    public void atualizarSocket(Socket novoSocket) {
        this.socket = novoSocket;
    }  
    
    
    public void atualizarToken(String novoToken) {
        this.token = novoToken;
    } 
}
