package entrega1;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

public class Servidor {
    private JTextArea logTextArea;
    private JTextArea userListTextArea;
    private JSONObject[] cadastros;
    private int portNumber; 
    private JSONObject userTokens[];

    public Servidor() {
    	cadastros = new JSONObject[50]; //vetor de clientes
    	
        cadastros[0] = new JSONObject();
        cadastros[0].put("name", "admin");
        cadastros[0].put("email", "admin@admin.com");
        cadastros[0].put("type", "admin");
        cadastros[0].put("password", hashearSenha("E00CF25AD42683B3DF678C61F42C6BDA"));  //admin1   
        
        cadastros[1] = new JSONObject();
        cadastros[1].put("name", "user");
        cadastros[1].put("email", "user@user.com");
        cadastros[1].put("type", "user");
        cadastros[1].put("password", hashearSenha("E00CF25AD42683B3DF678C61F42C6BDA"));  //admin1  

        
    	userTokens = new JSONObject[50]; // vetor de tokens
       

        JFrame frame = new JFrame("Servidor Sistemas Distribuidos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700,300);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField portField = new JTextField(5);
        portField.setText(String.valueOf(portNumber)); // Define a porta padrão
        JButton saveButton = new JButton("Salvar Porta");
        JButton clearButton = new JButton("Limpar Log"); // Botão para limpar o log
        JButton tokensButton = new JButton("Mostrar Tokens");
        controlPanel.add(new JLabel("Porta:"));
        controlPanel.add(portField);
        controlPanel.add(saveButton);
        controlPanel.add(clearButton);
        controlPanel.add(tokensButton);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        
        userListTextArea = new JTextArea();
        userListTextArea.append("Usuarios conectados:"+"\n");
        userListTextArea.setEditable(false); 
        JScrollPane userListScrollPane = new JScrollPane(userListTextArea);
        userListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Adicione uma barra de rolagem vertical
        

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, userListScrollPane);
        frame.add(controlPanel, BorderLayout.SOUTH);
 
        splitPane.setResizeWeight(0.8); 
        frame.add(splitPane, BorderLayout.CENTER);

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

        tokensButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e){                
                listarTokens();
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
    
    private void listarTokens(){
        log("Tokens no servidor: ");
        for (int i = 0; i < userTokens.length; i++) {            
          if (userTokens[i] != null){
            log(userTokens[i].toString());
        }else{
            log("Fim!");
            return;
            }
        }
    }
     
    private boolean validarLogin(String usuario, String senha) {
    	    for (JSONObject cadastro : cadastros) {
    	        if (cadastro != null && usuario.equals(cadastro.optString("email", ""))
    	                && BCrypt.checkpw(senha, cadastro.optString("password", ""))) {
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
    
    private boolean isAdmin(String usuario) {
    	 for (JSONObject cadastro : cadastros) {
             if (cadastro != null && usuario.equals(cadastro.optString("email", ""))
                     && ("admin").equals(cadastro.optString("type", ""))) {
                 return true; // Usuário e senha válidos
             }
         }
         return false;
    }
    
    private String buscarEmail(int userId) {    	   
        JSONObject cadastro = cadastros[userId];
        if (cadastro != null ) 
            return cadastro.optString("email", "");
        else 
        	return "";
    }

    private void addUserToken(String token, InetAddress ip){
    	int posicao = encontrarPosicaoVaziaTokens(); 
    	String email = buscarEmail(Integer.parseInt(JwtUtil.getUserIdFromToken(token)));
	     JSONObject novo = new JSONObject();
	     novo.put("usuarioId", JwtUtil.getUserIdFromToken(token));
	     novo.put("token", token);
	     novo.put("email", email);
	     novo.put("ip", ip);
	     novo.put("only-logout", false);
	     userTokens[posicao] = novo;   
	     userListTextArea.append(email+ip+ "\n");
   }
    
    private void deleteUserToken(String token) {

		for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null && userTokens[i].optString("usuarioId","").equals(JwtUtil.getUserIdFromToken(token))) {
                String text = userListTextArea.getText();
                String newText = text.replaceAll(userTokens[i].optString("email", "") +userTokens[i].optString("ip", "") + "\n", ""); // Remova o usuário da lista
                userListTextArea.setText(newText);
            	userTokens[i]= null;
            	break;
            }    		
		}
    }
    private void setOnlyLogoutAllowed(String token) {
    	for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null && userTokens[i].optString("usuarioId","").equals(JwtUtil.getUserIdFromToken(token))) {
            	userTokens[i].put("only-logout",true);
            	break;
            }    		
		}
    }
    
    private boolean isOnlyLogoutAllowed(String token) {
    	Boolean  onlyLogout = false;
    
    	for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null && userTokens[i].optString("usuarioId","").equals(JwtUtil.getUserIdFromToken(token))) {
            	onlyLogout = userTokens[i].getBoolean("only-logout");
            	break;
            }   
    	}

    	return onlyLogout; 
    	
    }
    
    private boolean isOnUserToken(String token) {
    	for (int i = 0; i < userTokens.length; i++) {
            if (userTokens[i] != null && userTokens[i].optString("token","").equals(token)) 
                return true;       	    
    	}
    	return false;
    }
    
    private String buscarTokenPeloIp(InetAddress ip) {
    	String token = "";
    	for (JSONObject userToken : userTokens) {
            if (userToken != null && (ip.toString()).equals(userToken.optString("ip", ""))) {
                token = userToken.optString("token", ""); 
                return token;
            }
        }
        return token;
    }
   
    private boolean isValidToken(String token) {
    	for (JSONObject userToken : userTokens) {
            if (userToken != null && token.equals(userToken.optString("token", ""))) {
                return true; // token e id válidos
            }
        }
        return false;     
    }
    
    private String hashearSenha(String senha) {
    	return BCrypt.hashpw(senha, BCrypt.gensalt());
    }

    private boolean isValidEmail(String email) {
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
    private boolean isEmailAlreadyRegistered(String email) {
        for (JSONObject cadastro : cadastros) {
            if (cadastro != null && email.equals(cadastro.optString("email", ""))) {
                return true; // E-mail já registrado
            }
        }
        return false; // E-mail não encontrado
    }
       
    private class ServerListener implements Runnable {
        private int portNumber;

        public ServerListener(int portNumber) {
            this.portNumber = portNumber;
        }

        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            	InetAddress serverAddress = InetAddress.getLocalHost();
                log("Servidor aguardando conexões no Ip/Porta: " + serverAddress.getHostAddress() + "/" + portNumber + " ...");
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
                        case "login": {
                            if (dataIn != null) {
                                String email = dataIn.optString("email", "");
                                String senha = dataIn.optString("password", "");
                                if (email.isEmpty()) {
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", "login");
                                    resposta.put("error", true);
                                    resposta.put("message", "Informe um e-mail.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                if (senha.isEmpty()) {
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", "login");
                                    resposta.put("error", true);
                                    resposta.put("message", "Informe a senha.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (!isValidEmail(email)) {
                                    // Se o e-mail não for válido, responda ao cliente com um erro
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "login");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail inválido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }                                
                                if (validarLogin(email, senha)) {
                                	
                                	String token = JwtUtil.generateToken(String.valueOf(buscarId(email)), isAdmin(email));  
                                	addUserToken(token, clientSocket.getInetAddress());
                                	
                                	JSONObject resposta = new JSONObject();
                                	resposta.put("action","login");
                                	resposta.put("error","false");
                                	resposta.put("message","Login efetuado com sucesso!");
                                	JSONObject data = new JSONObject();
                                	data.put("token",token);
                                	resposta.put("data", data);
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress()+": " + resposta);
                                    out.println(resposta);

                                    break;
                                } else {
                                    JSONObject resposta = new JSONObject();
                                	resposta.put("action","login");
                                	resposta.put("error","true");
                                	resposta.put("message","Usuario ou Senha Invalidos!\n Tente Novamente");
                                	JSONObject data = new JSONObject();
                                	data.put("token","");
                                	resposta.put("data", data);
                                    out.println(resposta);
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress()+": " + resposta);
                                    break;
                                   
                                }
                            } else {
                            	JSONObject resposta = new JSONObject();
                                resposta.put("action", "login");
                                resposta.put("error", true);
                                resposta.put("message", "Campo 'data' está vazio.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                
                            }
                    
                            break;
                        }
                        case "logout":{
                            if (dataIn != null) {
                                String token = dataIn.optString("token", "");
                                if (isValidToken(token)) {    
                                    deleteUserToken(token);
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
                                    resposta.put("message", "Token invalido. O logout falhou.");
                                    out.println(resposta.toString());
                                    log("Logout falhou no: "+ clientSocket.getInetAddress());
                                }
                            }
                            else {
                                JSONObject resposta = new JSONObject();
                                resposta.put("action", "logout");
                                resposta.put("error", true);
                                resposta.put("message", "Informe a senha.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                                }
                            break;                            
                        }
                        case "cadastro-usuario":
                            if (dataIn != null) {
                                String nome = dataIn.optString("name", "");
                                String token = dataIn.optString("token", "");
                                String email = dataIn.optString("email", "");
                                String tipo = dataIn.optString("type", "");
                                String senha = dataIn.optString("password", "");
                                
                                if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }

                                // Validação de entrada
                                if (nome.isEmpty() || email.isEmpty() || tipo.isEmpty() || senha.isEmpty()) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Dados de entrada incompletos.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                
                                if (!isValidEmail(email)) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail inválido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (isEmailAlreadyRegistered(email)) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail ja cadastrado.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }

                                if (!JwtUtil.isUserAdmin(token) && ("admin").equals(tipo)) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Usuário sem permissão para cadastrar administradores.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                } else {
                                    Integer usuarioId = encontrarPosicaoVaziaCadastros();

                                    if (usuarioId >= 0 && usuarioId < cadastros.length) {
                                        JSONObject novoCadastro = new JSONObject();
                                        novoCadastro.put("name", nome);
                                        novoCadastro.put("email", email);
                                        novoCadastro.put("type", tipo);
                                        novoCadastro.put("password", hashearSenha(senha));
                                        cadastros[usuarioId] = novoCadastro;

                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "cadastro-usuario");
                                        resposta.put("error", false);
                                        resposta.put("message", "Usuário cadastrado	 com sucesso!");
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                        out.println(resposta.toString());

                                    } else {
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "cadastro-usuario");
                                        resposta.put("error", true);
                                        resposta.put("message", "Falha ao cadastrar. Sem espaço para armazenar.");
                                        out.println(resposta.toString());
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);

                                    }
                                }
                            }else {
                            	JSONObject resposta = new JSONObject();
                                resposta.put("action", "cadastro-usuario");
                                resposta.put("error", true);
                                resposta.put("message", "Campo 'data' vazio.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                break;
                            }
                            break;
                            
                        case "autocadastro-usuario":
                            if (dataIn != null) {
                                String nome = dataIn.optString("name", "");
                                String email = dataIn.optString("email", "");
                                String tipo = "user";
                                String senha = dataIn.optString("password", "");
                                

                                if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "autocadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Dados de entrada incompletos.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                    break;
                                }
                                if (!isValidEmail(email)) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "autocadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail invalido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (isEmailAlreadyRegistered(email)) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "autocadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail ja cadastrado.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }

                                    Integer usuarioId = encontrarPosicaoVaziaCadastros();

                                    if (usuarioId >= 0 && usuarioId < cadastros.length) {
                                        JSONObject novoCadastro = new JSONObject();
                                        novoCadastro.put("name", nome);
                                        novoCadastro.put("email", email);
                                        novoCadastro.put("type", tipo);
                                        novoCadastro.put("password", hashearSenha(senha));
                                        cadastros[usuarioId] = novoCadastro;

                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "autocadastro-usuario");
                                        resposta.put("error", false);
                                        resposta.put("message", "Usuário cadastrado com sucesso!");
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                        out.println(resposta.toString());

                                    } else {
                                        // Responde ao cliente com erro se o vetor não tem posição válida
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "autocadastro-usuario");
                                        resposta.put("error", true);
                                        resposta.put("message", "Falha ao cadastrar. Sem espaço para armazenar.");
                                        out.println(resposta.toString());
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);

                                    }
                            }
                                    
							else {
								JSONObject resposta = new JSONObject();
								resposta.put("action", "autocadastro-usuario");
								resposta.put("error", true);
								resposta.put("message", "Falha! Campo 'data' vazio.");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
								break;
							}
							                            
                            
                            break;


                            
                        case "listar-usuarios":
                            if (dataIn != null) {
                                String token = dataIn.optString("token", ""); // Obtém o token do usuário
                                JSONObject resposta = new JSONObject();
                                
                                if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (token.isEmpty()) {
                                    resposta.put("action", "listar-usuarios");
                                    resposta.put("error", true);
                                    resposta.put("message", "Campo 'token' vazio");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                
                                if(!JwtUtil.isUserAdmin(token)) {
                                	
                                	resposta.put("action", "listar-usuarios");
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuario sem permissao");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                
                                JSONArray usuariosArray = new JSONArray();                                
                                for (int i = 0; i < cadastros.length; i++) {
                                    JSONObject usuario = cadastros[i];
                                    if (usuario != null) {
                                        JSONObject usuarioData = new JSONObject();
                                        usuarioData.put("id", i);
                                        usuarioData.put("name", usuario.optString("name", ""));
                                        usuarioData.put("type", usuario.optString("type", ""));
                                        usuarioData.put("email", usuario.optString("email", ""));
                                        usuariosArray.put(usuarioData);
                                    }
                                }

                                JSONObject dataResposta = new JSONObject();
                                resposta.put("action", "listar-usuarios");
                                resposta.put("error", false);
                                resposta.put("message", "Dados Listados com Sucesso!");
                                dataResposta.put("users", usuariosArray);
                                resposta.put("data", dataResposta);
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                out.println(resposta.toString());
                                } else {
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", "listar-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao listar. Campo 'data' não encontrado.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                            
                            break;
                            
                        case "pedido-proprio-usuario":{
                        	if (dataIn != null) {
                                String token = dataIn.optString("token", ""); // Obtém o token do usuário
                                JSONObject resposta = new JSONObject();
                                
                                if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (token.isEmpty()) {
                                    resposta.put("action", "pedido-proprio-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Campo 'token' vazio");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                resposta.put("action", "pedido-proprio-usuario");
                                resposta.put("error", false); 
                                resposta.put("message", "Sucesso");                                
                               	
                            	Integer id = Integer.parseInt(JwtUtil.getUserIdFromToken(token));
                            	JSONObject usuarioData = new JSONObject();
                                usuarioData.put("id", id);
                                usuarioData.put("name", cadastros[id].optString("name", ""));
                                usuarioData.put("type", cadastros[id].optString("type", ""));
                                usuarioData.put("email", cadastros[id].optString("email", ""));
                                JSONObject dataResposta = new JSONObject();
                                resposta.put("action", "pedido-proprio-usuario");
                                resposta.put("error", false);
                                resposta.put("message", "Seus dados listados com sucesso! \nMotivo: Usuário: tipo 'user'!");
                                dataResposta.put("user", usuarioData);
                                resposta.put("data", dataResposta);
                                    
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                        	} else {
                        		JSONObject resposta = new JSONObject();
                                resposta.put("action", "pedido-proprio-usuario");
                                resposta.put("error", true);
                                resposta.put("message", "Falha. Campo 'data' não encontrado.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                break;
                            }
                        }
                        	break;

                            
                        case "pedido-edicao-usuario":{
                            if (dataIn != null) {
                            	String token = dataIn.optString("token", "");
                                Integer usuarioId = dataIn.optInt("user_id");
                                
                                if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                
                                JSONObject resposta = new JSONObject();
                                resposta.put("action", "pedido-edicao-usuario");
                                if (!JwtUtil.isUserAdmin(token)) {
                                	resposta.put("error", true);
                                    resposta.put("message", "Usuario sem permissao");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                if (usuarioId > cadastros.length || cadastros[usuarioId] == null || usuarioId < 0) { 
                                	resposta.put("error", true);
                                    resposta.put("message", "Usuário não encontrado");
                                    resposta.put("data", "");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;                    
                                }
                                JSONObject dadosEncontrados = new JSONObject();
                                dadosEncontrados.put("name", cadastros[usuarioId].optString("name", ""));
                                dadosEncontrados.put("email", cadastros[usuarioId].optString("email", ""));
                                dadosEncontrados.put("type", cadastros[usuarioId].optString("type", ""));
                                dadosEncontrados.put("id", usuarioId);
                                                                                                
                                if (dadosEncontrados.isEmpty()) {
                                	resposta.put("error", true);
                                    resposta.put("message", "Usuário encontrado encontrado porém sem informacoes no cadastro");
                                    resposta.put("data", dadosEncontrados);
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;     
                                }
                                JSONObject user = new JSONObject();
                                user.put("user", dadosEncontrados);                                		
                                resposta.put("error", false);
                                resposta.put("message", "Sucesso!");
                                resposta.put("data", user);
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                            }
                            else {
                            	JSONObject resposta = new JSONObject();
                            	resposta.put("error", true);
                                resposta.put("message", "Erro! Campo 'data' está vazio.");
                                resposta.put("data", "");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                break;
                            }
                            break;
                        }
                            
                            

						case "edicao-usuario":{
			                JSONObject resposta = new JSONObject();
							if (dataIn == null) {
					                resposta.put("action", "edicao-usuario");
					                resposta.put("error", true);
					                resposta.put("message", "Erro!. Campo 'data' esta vazia");
					                out.println(resposta.toString());
					                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
					            }

							 String token = dataIn.optString("token", "");
							 
							 if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
							 
							 String nome = dataIn.optString("name", "");
                             String email = dataIn.optString("email", "");
                             String tipo = dataIn.optString("type", "");
                             String senha = dataIn.optString("password", "");
                             Integer usuarioId = dataIn.optInt("user_id");
                             if(token.isEmpty()) {
                            	resposta.put("action", "edicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Erro. Token não informado");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
                             }
                             if(nome.isEmpty()) {
                             	resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Nome não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(email.isEmpty()) {
                             	resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Email não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(tipo.isEmpty()) {
                             	resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Tipo de usuário não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if((usuarioId.toString()).isEmpty()) {
                             	resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Id do usuário não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             } 
                             if (usuarioId == 0 )  {
 					            resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Falha! Você não pode alterar o usuário admin@admin.com");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
 					        }

					        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
					            resposta.put("action", "edicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Usuário não encontrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
					        
					        if (!isValidEmail(email)){
					            resposta.put("action", "edicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Email invalido");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
					        if ((!(cadastros[usuarioId].optString("email", "")).equals(email)) && isEmailAlreadyRegistered(email)) {						        	
								resposta.put("action", "edicao-usuario");
								resposta.put("error", true);
								resposta.put("message", "Email informado ja esta sendo utilizado em outro cadastro");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;															        	
					        }
			               if((!senha.isEmpty()) && (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) == usuarioId)) {
			                	if (BCrypt.checkpw(senha, cadastros[usuarioId].optString("password", ""))){
			                		resposta.put("action", "edicao-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "A senha nova deve ser diferente da atual.");
						            out.println(resposta.toString());
						            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
			                	}
			                	cadastros[usuarioId].put("password", hashearSenha(senha));
			                } else if((!senha.isEmpty()) && (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) != usuarioId)){
			                	resposta.put("action", "edicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Não autorizado! Você pode alterar somente sua própria senha.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
			                }
			               if (JwtUtil.getUserIdFromToken(token).equals(usuarioId.toString()) && (!tipo.equals(cadastros[usuarioId].optString("type","")))) {
			            	   cadastros[usuarioId].put("name", nome);
			            	   cadastros[usuarioId].put("type", tipo);
			            	   cadastros[usuarioId].put("email", email);
			            	   resposta.put("action", "edicao-usuario");
					           resposta.put("error", false);
					           resposta.put("message", "Sucesso! Você alterou suas permissões. Favor fazer login novamente.");
					           out.println(resposta.toString());
					           log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					           setOnlyLogoutAllowed(token);
				               break;
			               }
							cadastros[usuarioId].put("name", nome);
							cadastros[usuarioId].put("type", tipo);
							cadastros[usuarioId].put("email", email);
			                
			                resposta.put("action", "edicao-usuario");
			                resposta.put("error", false);
			                resposta.put("message", "Alterações salvas com sucesso.");
			                out.println(resposta.toString());
			                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
			                if (isOnUserToken(token)) {
					        	deleteUserToken(token);
					        	addUserToken(token, clientSocket.getInetAddress());
					        }						        
						} 
						break;
						
						case "autoedicao-usuario":{
			                JSONObject resposta = new JSONObject();
							if (dataIn == null) {
								resposta.put("action", "autoedicao-usuario");
								resposta.put("error", true);
								resposta.put("message", "Chave 'data' esta vazia");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
							}							
							String token = dataIn.optString("token", "");
							
							if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                            }
							
							String nome = dataIn.optString("name", "");
							String email = dataIn.optString("email", "");
							String senha = dataIn.optString("password", "");
							Integer usuarioId = dataIn.optInt("id");
							if( usuarioId != Integer.parseInt(JwtUtil.getUserIdFromToken(token))) {
                            	resposta.put("action", "autoedicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Erro. Id informado é diferente do id do token informado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
                             }
							if(token.isEmpty()) {
                            	resposta.put("action", "autoedicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Erro. Token não informado");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
                             }
                             if(nome.isEmpty()) {
                             	resposta.put("action", "autoedicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Nome não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(email.isEmpty()) {
                             	resposta.put("action", "autoedicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Email não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if((usuarioId.toString()).isEmpty()) {
                             	resposta.put("action", "autoedicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Id do usuário não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }

					        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
					            resposta.put("action", "autoedicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Usuário não encontrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
					        if (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) != usuarioId) {
				                resposta.put("action", "autoedicao-usuario");
				                resposta.put("error", true);
				                resposta.put("message", "Você só pode alterar sua senha");
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
				            }

					        if (!isValidEmail(email)){
					            resposta.put("action", "autoedicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Email invalido");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
					        if ((!(cadastros[usuarioId].optString("email", "")).equals(email))&& isEmailAlreadyRegistered(email)) {						        	
								resposta.put("action", "autoedicao-usuario");
								resposta.put("error", true);
								resposta.put("message", "Email informado ja esta sendo utilizado em outro cadastro");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;															        	
					        }						        
			                cadastros[usuarioId].put("name", nome);
			                cadastros[usuarioId].put("email", email);
			                if(!senha.isEmpty()) {	
			                	if (BCrypt.checkpw(senha, cadastros[usuarioId].optString("password", ""))){
			                		resposta.put("action", "edicao-usuario");
						            resposta.put("error", true);
						            resposta.put("message", "A senha nova deve ser diferente da atual.");
						            out.println(resposta.toString());
						            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
					                break;
				                }
				                cadastros[usuarioId].put("password", hashearSenha(senha));
			                }
			                resposta.put("action", "autoedicao-usuario");
			                resposta.put("error", false);
			                resposta.put("message", "Alterações salvas com sucesso.");
			                out.println(resposta.toString());
			                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
			                if (isOnUserToken(token)) {
					        	deleteUserToken(token);
					        	addUserToken(token, clientSocket.getInetAddress());
					        }
			            } 
						break;
						    
						case "excluir-usuario":{
							if (dataIn == null) {
				                JSONObject resposta = new JSONObject();
				                resposta.put("action", "excluir-usuario");
				                resposta.put("error", true);
				                resposta.put("message", "Chave 'data' esta vazia");
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
							}

							 String token = dataIn.optString("token", "");
							 
							 if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                             	JSONObject resposta = new JSONObject();
                                 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
							 
                             Integer usuarioId = dataIn.optInt("user_id");
					         JSONObject resposta = new JSONObject();
					         
					        if ((usuarioId.toString()).isEmpty()) {
					        	resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Falha! Id do usuário não informado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
					        if (!JwtUtil.isUserAdmin(token)) {					        	
					            resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Usuário sem permissão !");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
					        if (usuarioId == 0) {
					        	resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Erro! Você não pode excluir o usuário admin@admin.com");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
					        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
					            resposta.put("action", "excluir-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Usuário não encontrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }					
					        
			                resposta.put("action", "excluir-usuario");
			                resposta.put("error", false);
			                if (usuarioId == Integer.parseInt(JwtUtil.getUserIdFromToken(token)) ){
			                	resposta.put("message", "Excluiu seu próprio usuário com sucesso! Você sera desconectado.");
					        	out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                cadastros[usuarioId]= null;
				                setOnlyLogoutAllowed(token);
					        	break;
			                }else {
						    	resposta.put("message", "Usuário excluido com sucesso.");			                
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                cadastros[usuarioId]= null;
			                }
			            
						}
                    
						break;
						
						case "excluir-proprio-usuario":{
							if (dataIn == null) {
				                JSONObject resposta = new JSONObject();
				                resposta.put("action", "excluir-proprio-usuario");
				                resposta.put("error", true);
				                resposta.put("message", "Falha! Chave 'data' esta vazia");
				                out.println(resposta.toString());
				                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
							}

							 String token = dataIn.optString("token", "");
							 
							 if(isOnUserToken(token) && isOnlyLogoutAllowed(token)){
                             	JSONObject resposta = new JSONObject();
                                 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
							 
                             String email = dataIn.optString("email", "");
                             String senha = dataIn.optString("password", "");
                             Integer usuarioId = buscarId(email);
					         JSONObject resposta = new JSONObject();
					         if(token.isEmpty()) {
	                        	resposta.put("action", "excluir-proprio-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Erro. Token não informado");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					         }
                             if(email.isEmpty()) {
                             	resposta.put("action", "excluir-proprio-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Email não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(senha.isEmpty()) {
                             	resposta.put("action", "excluir-proprio-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Senha do usuário não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }

					        if (usuarioId >= 0 && usuarioId < cadastros.length && cadastros[usuarioId] == null)  {
					            resposta.put("action", "excluir-proprio-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Falha! Usuário não encontrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }					
					        if (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) != usuarioId) {					        	
					            resposta.put("action", "excluir-proprio-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Falha! Email diferente do cadastrado.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        } 
					        if (!validarLogin(email, senha)) {
					        	resposta.put("action", "excluir-proprio-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Falha! senha incorreta.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
					        }
			                resposta.put("action", "excluir-proprio-usuario");
			                resposta.put("error", false);			                
		                	resposta.put("message", "Excluiu seu próprio usuário com sucesso! Você sera desconectado.");
				        	setOnlyLogoutAllowed(token);	
				        	out.println(resposta.toString());
			                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
			                cadastros[usuarioId]= null;
				        	break;
			                }
                            
						default:
							
							break;
                }
                    } log("Servidor: Cliente desconectado: " + clientSocket.getInetAddress()+ ":"+ clientSocket.getLocalPort()+"/");
                    deleteUserToken(buscarTokenPeloIp(clientSocket.getInetAddress()));
                  
            } catch (IOException e) {
            	log("Servidor: Cliente desconectou no Ip:" + clientSocket.getInetAddress() + " mensagem: "+ e.getMessage());
            	deleteUserToken(buscarTokenPeloIp(clientSocket.getInetAddress()));
                
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
