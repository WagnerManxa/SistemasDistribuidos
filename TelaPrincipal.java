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
        setSize(400, 400);

        
        JLabel usuariosLabel = new JLabel("Usuários");
        
        JButton cadastrarUsuarioButton = new JButton("Cadastrar");
        cadastrarUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirCadastroUsuario();
            }
        });
        
        JButton informacaoUsuarioButton = new JButton("Informações");
        informacaoUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarUsuarios();
            }
        });
               
        JButton editarUsuarioButton = new JButton("Editar");
        editarUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirEditarUsuario();
            }
        });
        
        JButton excluirUsuarioButton = new JButton("Excluir");
        excluirUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                excluirUsuario();
            }
        });
 
        /////////////////////////////////////////////////////////////////////
        
        JLabel pontosLabel = new JLabel("Pontos");
        
        JButton cadastrarPontoButton = new JButton("Cadastrar");
        cadastrarPontoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	abrirCadastroPonto();
            }
        });

        JButton informacaoPontoButton = new JButton("Informações");
        informacaoPontoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarPontos();
            }
        });
               
        JButton editarPontoButton = new JButton("Editar");
        editarPontoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	abrirEditarPonto();
            }
        });
                      
        JButton excluirPontoButton = new JButton("Excluir");
        excluirPontoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                excluirPonto();
            }
        });
        
        ////////////////////////////////////////////////////////////////
        
        JLabel segmentosLabel = new JLabel("Segmentos");
        
        JButton cadastrarSegmentoButton = new JButton("Cadastrar");
        cadastrarSegmentoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	abrirCadastroSegmento();
            }
        });

        JButton informacaoSegmentoButton = new JButton("Informações");
        informacaoSegmentoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              listarSegmentos();
            }
        });
               
        JButton editarSegmentoButton = new JButton("Editar");
        editarSegmentoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	abrirEditarSegmento();
            }
        });
                      
        JButton excluirSegmentoButton = new JButton("Excluir");
        excluirSegmentoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //excluirSegmento();
            }
        });
                
        //////////////////////////////////////////////////
        JLabel rotasLabel = new JLabel("Rotas");
        
        JButton cadastrarRotaButton = new JButton("Cadastrar");
        cadastrarRotaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            //	abrirCadastroRota();
            }
        });

        JButton informacaoRotaButton = new JButton("Informações");
        informacaoRotaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              //  listarRotas();
            }
        });
               
        JButton editarRotasButton = new JButton("Editar");
        editarRotasButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//abrirEditarRota();
            }
        });
                      
        JButton excluirRotasButton = new JButton("Excluir");
        excluirRotasButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              //  excluirRota();
            }
        });
        
        //////////////////////////////////////////////////
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enviarLogout(token); 
            }
        });
                       


        JPanel tudo = new JPanel(new FlowLayout());

        JPanel panel0 = new JPanel(new FlowLayout());
        
        panel0.add(cadastrarUsuarioButton);
        panel0.add(editarUsuarioButton);
        panel0.add(informacaoUsuarioButton);         
        panel0.add(excluirUsuarioButton);
        panel0.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), usuariosLabel.getText()));
        
        JPanel panel1 = new JPanel(new FlowLayout());
        panel1.add(cadastrarPontoButton);
        panel1.add(editarPontoButton);
        panel1.add(informacaoPontoButton);
        panel1.add(excluirPontoButton);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),pontosLabel.getText()));
        
        JPanel panel2 = new JPanel(new FlowLayout());
        panel2.add(cadastrarSegmentoButton);
        panel2.add(editarSegmentoButton);
        panel2.add(informacaoSegmentoButton);
        panel2.add(excluirSegmentoButton);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),segmentosLabel.getText()));
        
        JPanel panel3 = new JPanel(new FlowLayout());
        panel3.add(cadastrarRotaButton);
        panel3.add(editarRotasButton);
        panel3.add(informacaoRotaButton);
        panel3.add(excluirRotasButton);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),rotasLabel.getText()));
        JPanel panel4 = new JPanel(new FlowLayout());
        panel4.add(logoutButton);
        
        tudo.add(panel0);
        tudo.add(panel1);
        tudo.add(panel2);
        tudo.add(panel3);
        tudo.add(panel4);
        add(tudo);
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
    private void listarPontos() {
    	InformacaoPonto listarPontos = new InformacaoPonto(socket,this, this.token);
    	listarPontos.atualizarToken(this.token);
    	listarPontos.setVisible(true);

    }
    
    private void excluirPonto() {
    	ExcluirPonto excluirPontos = new ExcluirPonto(socket,this, this.token);
    	excluirPontos.atualizarToken(this.token);
    	excluirPontos.setVisible(true);

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

    private void abrirCadastroPonto() {
        CadastroPonto cadastroPonto = new CadastroPonto(socket, this,token);
        cadastroPonto.atualizarToken(this.token);
        cadastroPonto.setVisible(true);
        this.dispose();
    }
    
    private void abrirEditarPonto() {
        EditarPonto editarPonto = new EditarPonto(socket, this,token);
        editarPonto.atualizarToken(this.token);
        editarPonto.setVisible(true);
        this.dispose();
    }
    
    private void abrirEditarSegmento() {
        EditarSegmento editarSegmento = new EditarSegmento(socket, this, this.token);
        editarSegmento.atualizarToken(this.token);
        editarSegmento.setVisible(true);
        this.dispose();
    }
    
    private void abrirCadastroSegmento() {
        CadastroSegmento cadastroSegmento = new CadastroSegmento(socket, this,token);
        cadastroSegmento.atualizarToken(this.token);
        cadastroSegmento.setVisible(true);
        this.dispose();
    }
    
    private void listarSegmentos() {
    	InformacaoSegmento listarSegmento = new InformacaoSegmento(socket,this, this.token);
    	listarSegmento.atualizarToken(this.token);
    	listarSegmento.setVisible(true);

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
            JOptionPane.showMessageDialog(this, "'token' vazio ou nulo. O logout falhou.");
            
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
		            String action = respostaJSON.optString("action");
		            Boolean error = respostaJSON.optBoolean("error");
		            String message = respostaJSON.optString("message");
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
