package entrega1;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Servidor {
    private JTextArea logTextArea;
    private JSONObject[] cadastros;
    private int portNumber; 
    private JSONObject userTokens[];

    public Servidor() {
    	userTokens = new JSONObject[100];
        cadastros = new JSONObject[100];
        cadastros[99] = new JSONObject();
        cadastros[99].put("nome", "admin");
        cadastros[99].put("email", "admin");
        cadastros[99].put("tipo", "admin");
        cadastros[99].put("senha", "e00cf25ad42683b3df678c61f42c6bda");       
        //E00CF25AD42683B3DF678C61F42C6BDA
        
        cadastros[1] = new JSONObject();
        cadastros[1].put("nome", "user");
        cadastros[1].put("email", "user");
        cadastros[1].put("tipo", "user");
        cadastros[1].put("senha", "e10adc3949ba59abbe56e057f20f883e");
        //E10ADC3949BA59ABBE56E057F20F883

        JFrame frame = new JFrame("Servidor Sistemas Distribuidos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550,250);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField portField = new JTextField(5);
        portField.setText(String.valueOf(portNumber)); // Define a porta padrão
        JButton saveButton = new JButton("Salvar Porta");
        JButton clearButton = new JButton("Limpar Log"); // Botão para limpar o log
        controlPanel.add(new JLabel("Porta:"));
        controlPanel.add(portField);
        controlPanel.add(saveButton);
        controlPanel.add(clearButton);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int newPort = Integer.parseInt(portField.getText());
                    if (newPort > 0 && newPort < 65536) { // Verifica se a porta está dentro dos limites válidos
                        portNumber = newPort;
                        log("Porta salva: " + portNumber);
                        Thread serverThread = new Thread(new ServerListener(portNumber));
                        serverThread.start();
                    } else {
                        log("Porta inválida. A porta deve estar entre 1 e 65535.");
                    }
                } catch (NumberFormatException ex) {
                    log("Porta inválida. Certifique-se de que seja um número válido.");
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.setText(""); // Limpa o conteúdo da área de log
            }
        });
    
}
    

    
    private int encontrarPosicaoVaziaCadastros() {
        for (int i = 0; i < cadastros.length; i++) {
            if (cadastros[i] == null) {
                return i; // Retorna a primeira posição vazia encontrada
            }
        }
        return -1; // Retorna -1 se o vetor estiver cheio
    }
    
    private int encontrarPosicaoVaziaTokens() {
        for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] == null) {
                return i; // Retorna a primeira posição vazia encontrada
            }
        }
        return -1; // Retorna -1 se o vetor estiver cheio
    }

    private boolean validarLogin(String usuario, String senha) {
        for (JSONObject cadastro : cadastros) {
            if (cadastro != null && usuario.equals(cadastro.optString("email", ""))
                    && senha.equals(cadastro.optString("senha", ""))) {
                return true; // Usuário e senha válidos
            }
        }
        return false; // Usuário e senha não encontrados
    }
    
    private int buscarId(String usuario) {
    	int id = 0;
        for (int i = 0; i < cadastros.length; i++) {
            JSONObject cadastro = cadastros[i];
            if (cadastro != null && usuario.equals(cadastro.optString("email", ""))) {
                id= i; 
            }
        }
        return id;
    }
    
    public boolean isAdmin(String usuario) {
    	 for (JSONObject cadastro : cadastros) {
             if (cadastro != null && usuario.equals(cadastro.optString("email", ""))
                     && ("admin").equals(cadastro.optString("tipo", ""))) {
                 return true; // Usuário e senha válidos
             }
         }
         return false;
    }
    
    public void addUserToken(String token){
    	 int posicao = encontrarPosicaoVaziaTokens();  	
	     JSONObject novo = new JSONObject();
	     novo.put("usuarioId", JwtUtil.getUserIdFromToken(token));
	     novo.put("token", token);
	     userTokens[posicao] = novo;    	
    }
    
    private boolean isValidToken(String token) {
    	for (JSONObject userToken : userTokens) {
            if (userToken != null && token.equals(userToken.optString("token", ""))) {
                return true; // token e id válidos
            }
        }
        return false;     
    }
    
    private void deleteUserToken(String token) {

    		for (int i = 0; i < userTokens.length; i++) {
                if (userTokens[i] != null && userTokens[i].optString("usuarioId","").equals(JwtUtil.getUserIdFromToken(token))) {
                	userTokens[i]= null;
                	break;
                }    		

        }
    	
    }
    public void imprimeTokens() {
    	for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null ) {
            	System.out.println("Posicao: "+i +" "+userTokens[i].optString("usuarioId","")+": token:"+userTokens[i].optString("token",""));
            }    		

    }
    }
    
    private class ServerListener implements Runnable {
        private int portNumber;

        public ServerListener(int portNumber) {
            this.portNumber = portNumber;
        }

        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
                log("Servidor aguardando conexões na porta " + portNumber + "...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    log("Servidor: Cliente conectado: " + clientSocket.getInetAddress()+ ":"+ clientSocket.getLocalPort()+"/");
                    new ClientHandler(clientSocket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        

        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    JSONObject mensagem = new JSONObject(inputLine);
                    log("Servidor <-Recebida do cliente " + clientSocket.getInetAddress()+":"+ mensagem);
                    String action = mensagem.optString("action", "");
                    JSONObject dataIn = mensagem.optJSONObject("data");
                    
                    switch (action) {
                        case "login":
                            if (dataIn != null) {
                                String usuario = dataIn.optString("email", "");
                                String senha = dataIn.optString("senha", "");
                                
                                if (validarLogin(usuario, senha)) {                                	
                                	//int idUsuarioEncontrado = buscarId(usuario);                                	
                                	String token = JwtUtil.generateToken(String.valueOf(buscarId(usuario)), isAdmin(usuario));  
                                	addUserToken(token);
                                	//criando a resposta para o cliente
                                	JSONObject resposta = new JSONObject();
                                	resposta.put("action","login");
                                	resposta.put("error","false");
                                	resposta.put("message","logado com sucesso");
                                	JSONObject data = new JSONObject();
                                	data.put("token",token);
                                	resposta.put("data", data);
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress()+": " + resposta);
                                    out.println(resposta);

                                    break;
                                } else {
                                    log("Tentativa de login falhou no: "+clientSocket.getInetAddress());
                                    JSONObject resposta = new JSONObject();
                                	resposta.put("action","login");
                                	resposta.put("error","true");
                                	resposta.put("message","Usuario ou senha invalidos");
                                	JSONObject data = new JSONObject();
                                	data.put("token","");
                                	resposta.put("data", data);
                                    out.println(resposta);
                                    break;
                                   
                                }
                            }
                            break;
                        
                        case "logout":
                            if (dataIn != null) {
                                String token = dataIn.optString("token", "");
                                if (isValidToken(token)) {     

                                    // Removendo o token do usuário (logout)
                                    deleteUserToken(token);


                                    // Respondendo ao cliente com sucesso
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "logout");
                                    resposta.put("error", false);
                                    resposta.put("message", "Logout efetuado com sucesso.");
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress()+": " + resposta);
                                    out.println(resposta.toString());
                                } else {
                                     
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "logout");
                                    resposta.put("error", true);
                                    resposta.put("message", "Token inválido. O logout falhou.");
                                    out.println(resposta.toString());
                                    log("Logout falhou no: "+ clientSocket.getInetAddress());
                                }
                                
                            }
                            break;

                            
                            
                        case "cadastro-usuario":
                            if (dataIn != null) {
                                String nome = dataIn.optString("nome", "");
                                String token = dataIn.optString("token", "");
                                String email = dataIn.optString("email", "");
                                String tipo = dataIn.optString("tipo", "");
                                String senha = dataIn.optString("senha", "");

                                // Validação de entrada
                                if (nome.isEmpty() || email.isEmpty() || tipo.isEmpty() || senha.isEmpty()) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Dados de entrada incompletos.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                    break;
                                }

                                // Verifica permissões de usuário
                                if (!JwtUtil.isUserAdmin(token) && tipo.equals("admin")) {
                                    // Responde ao cliente com erro se o usuário não tem permissão para cadastrar um administrador
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Usuário sem permissão para cadastrar administradores.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                } else {
                                    int usuarioId = encontrarPosicaoVaziaCadastros();

                                    if (usuarioId >= 0 && usuarioId < cadastros.length) {
                                        // Cria um novo cadastro de usuário
                                        JSONObject novoCadastro = new JSONObject();
                                        novoCadastro.put("nome", nome);
                                        novoCadastro.put("email", email);
                                        novoCadastro.put("tipo", tipo);
                                        novoCadastro.put("senha", senha);
                                        cadastros[usuarioId] = novoCadastro;

                                        // Responde com sucesso ao cliente
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "cadastro");
                                        resposta.put("error", false);
                                        resposta.put("message", "Cadastro efetuado com sucesso!");
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                        out.println(resposta.toString());

                                    } else {
                                        // Responde ao cliente com erro se o vetor não tem posição válida
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "cadastro");
                                        resposta.put("error", true);
                                        resposta.put("message", "Falha ao cadastrar. Sem espaço para armazenar.");
                                        out.println(resposta.toString());
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);

                                    }
                                }
                            }
                            break;




                        case "pedido-edicao-usuario":
                            if (dataIn != null) {
                                int usuarioId = dataIn.optInt("usuario_id", -1); // Obtém o ID do usuário
                                JSONObject resposta = new JSONObject();
                                resposta.put("action", "pedido-edicao-usuario");

                                if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] != null) {
                                    JSONObject usuario = cadastros[usuarioId];
                                    JSONObject usuarioData = new JSONObject(usuario.toString());
                                    resposta.put("error", false);
                                    resposta.put("message", "Sucesso");
                                    resposta.put("data", usuarioData);
                                } else {
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário não encontrado");
                                }

                                out.println(resposta.toString());
                            }
                            break;
                            
                        case "listar-usuarios":
                            if (dataIn != null) {
                                String token = dataIn.optString("token", ""); // Obtém o token do usuário
                                JSONObject resposta = new JSONObject();
                                resposta.put("action", "listar-usuarios");
                                resposta.put("error", false); // Defina o status de erro como falso
                                resposta.put("message", "Sucesso");
                                
                                if(isAdmin(token)) {
                                // Construa a lista de usuários no formato desejado
                                JSONArray usuariosArray = new JSONArray();
                                
                                // Adicione os usuários à lista
                                for (int i = 0; i < cadastros.length; i++) {
                                    JSONObject usuario = cadastros[i];
                                    if (usuario != null) {
                                        JSONObject usuarioData = new JSONObject();
                                        usuarioData.put("id", i);
                                        usuarioData.put("nome", usuario.optString("nome", ""));
                                        usuarioData.put("tipo", usuario.optString("tipo", ""));
                                        usuarioData.put("email", usuario.optString("email", ""));
                                        usuariosArray.put(usuarioData);
                                    }
                                }

                                JSONObject dataResposta = new JSONObject();
                                dataResposta.put("usuarios", usuariosArray);
                                resposta.put("data", dataResposta);

                                // Envie a resposta de volta para o cliente
                                out.println(resposta.toString());
                                }
                            
                              }
                            break;
                            

						case "salvar-alteracoes-usuario":
						
							if (dataIn != null) {
						        int usuarioId = dataIn.optInt("usuario_id", -1);

						        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] != null) {
						            // Certifique-se de que a chave "novos_dados" existe antes de acessá-la
						            if (dataIn.has("novos_dados")) {
						                JSONObject novosDados = dataIn.getJSONObject("novos_dados");
						                
						                // Atualize os dados do cadastro com as informações recebidas
						                cadastros[usuarioId].put("nome", novosDados.optString("nome", ""));
						                cadastros[usuarioId].put("email", novosDados.optString("email", ""));
						                cadastros[usuarioId].put("tipo", novosDados.optString("tipo", ""));
						                cadastros[usuarioId].put("senha", novosDados.optString("senha", ""));
						                
						                // Envie uma resposta de sucesso ao cliente
						                JSONObject resposta = new JSONObject();
						                resposta.put("action", "salvar-alteracoes-usuario");
						                resposta.put("error", false);
						                resposta.put("message", "Alterações salvas com sucesso.");
						                out.println(resposta.toString());
						            } else {
						                // Envie uma resposta de erro ao cliente se "novos_dados" estiver ausente
						                JSONObject resposta = new JSONObject();
						                resposta.put("action", "salvar-alteracoes-usuario");
						                resposta.put("error", true);
						                resposta.put("message", "Chave 'novos_dados' ausente na mensagem.");
						                out.println(resposta.toString());
						            }
						        } else {
						            // Envie uma resposta de erro ao cliente se o usuário não for encontrado
						            JSONObject resposta = new JSONObject();
						            resposta.put("action", "salvar-alteracoes-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "Usuário não encontrado.");
						            out.println(resposta.toString());
						        }
						    }
						    break;
                            
                        default:
                            // Lida com ação desconhecida, se necessário
                            break;
                }
                    } log("Servidor: Cliente desconectado: " + clientSocket.getInetAddress()+ ":"+ clientSocket.getLocalPort()+"/");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }

    private void log(String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logTextArea.append(formattedDateTime + " - " + message + "\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Servidor();
            }
        });
    }
}
