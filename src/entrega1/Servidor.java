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
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

public class Servidor {
    private JTextArea logTextArea;
    private JTextArea userListTextArea;
    private JSONObject[] cadastros;
    private int portNumber; 
    private JSONObject userTokens[];
    private JSONObject pontos[];
    private JSONObject segmentos[];
    private final Integer tamanho = 50;

    public Servidor() {
    	
    	
    	///////////////clientes/////////////////////
    	cadastros = new JSONObject[tamanho]; //vetor de clientes
    	
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
        
        
        ///////////// lista de tokens ativos///////////////////////////
        
    	userTokens = new JSONObject[tamanho]; // vetor de tokens
    	
    	//////////////pontos///////////////
    	pontos = new JSONObject[200];// vetor de pontos
    	pontos[0]= new JSONObject();
    	pontos[0].put("name", "Portaria");
    	pontos[0].put("obs", "cuidado com os cães");
    	pontos[0].put("ponto_id", 0);
    	
    	pontos[1]= new JSONObject();
    	pontos[1].put("name", "bloco C");
    	pontos[1].put("obs", "cuidado com escada de acesso");
    	pontos[1].put("ponto_id", 1);
    	
    	pontos[2]= new JSONObject();
    	pontos[2].put("name", "R.U");
    	pontos[2].put("obs", "Restaurante Universitario");
    	pontos[2].put("ponto_id", 2);
    	
    	//////////////////segmentos/////////////////
    	
    	segmentos = new JSONObject[tamanho];  
    	
    	JSONObject pontoOrigem = new JSONObject();
    	pontoOrigem.put("id", 0);
    	pontoOrigem.put("name", pontos[0].optString("name"));
    	pontoOrigem.put("obs", pontos[0].optString("obs"));
    	
    	JSONObject pontoDestino = new JSONObject();
    	pontoDestino.put("id", 1);
    	pontoDestino.put("name", pontos[1].optString("name"));
    	pontoDestino.put("obs", pontos[1].optString("obs"));
    	
    	segmentos[0]= new JSONObject();
    	segmentos[0].put("id", 0);
    	segmentos[0].put("ponto_origem", pontoOrigem);
    	segmentos[0].put("ponto_destino", pontoDestino);
    	segmentos[0].put("distancia",50);
    	segmentos[0].put("direcao","Frente");
    	segmentos[0].put("obs", "Área com paralelepípedos");
    	
    	JSONObject pontoOrigem2 = new JSONObject();
    	pontoOrigem2.put("id", 1);
    	pontoOrigem2.put("name", pontos[1].optString("name"));
    	pontoOrigem2.put("obs", pontos[1].optString("obs"));
    	
    	JSONObject pontoDestino2 = new JSONObject();
    	pontoDestino2.put("id", 2);
    	pontoDestino2.put("name", pontos[2].optString("name"));
    	pontoDestino2.put("obs", pontos[2].optString("obs"));
    	
    	segmentos[1]= new JSONObject();
    	segmentos[1].put("id", 1);
    	segmentos[1].put("ponto_origem", pontoOrigem2);
    	segmentos[1].put("ponto_destino", pontoDestino2);
    	segmentos[1].put("distancia",50);
    	segmentos[1].put("direcao","Zig zag");
    	segmentos[1].put("obs", "Área complexada");
     	
    	      

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
    
    // Manter tokens//
    
    private int encontrarPosicaoVaziaTokens() {
        for (int i = 0; i < tamanho; i++) {
            if (userTokens[i] == null) {
                return i; // Retorna a primeira posição vazia encontrada
            }
        }
        return -1; // Retorna -1 se o vetor estiver cheio
    }
    
    private void listarTokens(){
        log("Tokens no servidor: ");
        for (int i = 0; i < tamanho; i++) {            
          if (userTokens[i] != null){
            log(userTokens[i].toString());
        }else{
            log("Fim!");
            return;
            }
        }
    }
     
    private void addUserToken(String token, InetAddress ip){
    	int posicao = encontrarPosicaoVaziaTokens(); 
    	String email = buscarEmailPeloId(Integer.parseInt(JwtUtil.getUserIdFromToken(token)));
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

		for (int i = 0; i < tamanho; i++) {
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
    	for (int i = 0; i < tamanho; i++) {
            if (userTokens[i] != null && userTokens[i].optString("usuarioId","").equals(JwtUtil.getUserIdFromToken(token))) {
            	userTokens[i].put("only-logout",true);
            	break;
            }    		
		}
    }
    
    private boolean isOnlyLogoutAllowed(String token) {
    	Boolean  onlyLogout = false;
    
    	for (int i = 0; i < tamanho; i++) {
            if (userTokens[i] != null && userTokens[i].optString("usuarioId","").equals(JwtUtil.getUserIdFromToken(token))) {
            	onlyLogout = userTokens[i].getBoolean("only-logout");
            	break;
            }   
    	}

    	return onlyLogout; 
    	
    }
    
    private boolean isTokenOnUserToken(String token) {
    	for (int i = 0; i < tamanho; i++) {
            if (userTokens[i] != null && userTokens[i].optString("token","").equals(token)) 
                return true;       	    
    	}
    	return false;
    }
    
    private boolean isIpOnUserToken(InetAddress ip) {
    	for (int i = 0; i < tamanho; i++) {
            if (userTokens[i] != null && userTokens[i].optString("ip","").equals(ip.toString())) 
                return true;       	    
    	}
    	return false;
    }
    
    private String buscarTokenPeloIp(InetAddress ip) {
    	for (JSONObject userToken : userTokens) {
            if (userToken != null && (ip.toString()).equals(userToken.optString("ip", ""))) {
                return userToken.optString("token");
            }
        }
        return null;
    }
    
    private String buscarTokenPeloId(Integer id) {

    	for (JSONObject userToken : userTokens) {
            if (userToken != null && (id.toString()).equals(userToken.optString("usuarioId", ""))) {
                return userToken.optString("token");
            }
        }
        return null;
    }

    private String buscarTokenPeloEmail(String email) {

    	for (JSONObject userToken : userTokens) {
            if (userToken != null && (email.equals(userToken.optString("email")))) {
                return userToken.optString("token");
            }
        }
        return null;
    }
    
    private InetAddress buscarIpPeloId(Integer id) throws Throwable {
    	InetAddress ip = null;
    	for (JSONObject userToken : userTokens) {
            if (userToken != null && (id.toString()).equals(userToken.optString("usuarioId", ""))) {
                ip = InetAddress.getByName(userToken.optString("ip")); 
                return ip;
            }
        }
        return ip;
    }
   
    // Manter Cadastros//
    
    private int encontrarPosicaoVaziaCadastros() {
        for (int i = 0; i < cadastros.length; i++) {
            if (cadastros[i] == null) {
                return i; // Retorna a primeira posição vazia encontrada
            }
        }
        return -1; // Retorna -1 se o vetor estiver cheio
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
    
    private Integer buscarId(String email) {
        for (int i = 0; i < cadastros.length; i++) {
            JSONObject cadastro = cadastros[i];
            if (cadastro != null && email.equals(cadastro.optString("email", ""))) {
                return i; 
            }
        }
        return null;
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
    
    private String buscarEmailPeloId(int userId) {    	   
        JSONObject cadastro = cadastros[userId];
        if (cadastro != null ) 
            return cadastro.optString("email");
        else 
        	return "";
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
       
    //Manter Pontos
    
	private Integer encontrarPosicaoVaziaPontos() {
		for (int i = 0; i < pontos.length; i++) {
			if (pontos[i] == null) {
				return i; 
			}
		}
		return null; 
	}
	
	/*private Integer buscarIdPontoPorNome(String name) {
		Integer ponto_id = null;
		for (int i = 0; i < pontos.length; i++) {
			JSONObject cadastro = pontos[i];
			if (cadastro != null && name.equals(cadastro.optString("name", ""))) {
				ponto_id= i; 
			}
		}
		return ponto_id;
	}*/
	
	private Boolean isNomePontoCadastrado(String name) {
		Boolean cadastrado = false;
		for (int i = 0; i < pontos.length; i++) {
			JSONObject cadastro = pontos[i];
			if (cadastro != null && name.equals(cadastro.optString("name", ""))) {
				cadastrado = true; 
			}
		}
		return cadastrado;
	}
	 
	private String buscarNomePontoPorId(Integer ponto_id) {
		String name = null;
		for (int i = 0; i < pontos.length; i++) {
			JSONObject cadastro = pontos[i];
			if (cadastro != null && ponto_id == (cadastro.optInt("ponto_id"))) {
				name = cadastro.optString("name", ""); 
			}
		}
		return name;
	}

	private JSONObject cadastrarPonto(String name, String obs){
		JSONObject resposta = new JSONObject();
		
		Integer posicao = encontrarPosicaoVaziaPontos(); 
		if (posicao == null) {
		resposta.put("action", "cadastro-ponto");
		resposta.put("error", true);			                
		resposta.put("message", "Erro, espaco insuficiente para adicionar ponto");
		return resposta;
		}
		JSONObject novo = new JSONObject();
		novo.put("name", name);
		novo.put("obs", obs);
		novo.put("ponto_id", posicao);
		pontos[posicao] = novo; 
		
		resposta.put("action", "cadastro-ponto");
		resposta.put("error", false);			                
		resposta.put("message", "Ponto cadastrado com sucesso");
		return resposta;
	}

	private JSONObject excluirPonto(Integer ponto_id) {
		JSONObject resposta = new JSONObject();
        if (pontos[ponto_id] == null) {
            resposta.put("action", "excluir-ponto");
    		resposta.put("error", true);			                
    		resposta.put("message", "Falha! Ponto nao encontrado");
        	return resposta;
        }
		pontos[ponto_id]= null;
		apagarSegmentoPorPonto(ponto_id);
		resposta.put("action", "excluir-ponto");
		resposta.put("error", false);			                
		resposta.put("message", "Ponto excluido com sucesso. Se o ponto estiver em um segmento, o mesmo tambem foi excluido.");
		return resposta;
	}

	private JSONObject listarPontos() {
		JSONObject resposta = new JSONObject();
		JSONArray pontosArray = new JSONArray();                                
		for (int i = 0; i < pontos.length; i++) {
			JSONObject ponto = pontos[i];
			if (ponto != null) {
				JSONObject pontoData = new JSONObject();
				pontoData.put("id", i);
				pontoData.put("name", ponto.optString("name", ""));
				pontoData.put("obs", ponto.optString("obs", ""));
				pontosArray.put(pontoData);
			}
		}

		JSONObject dataResposta = new JSONObject();
        dataResposta.put("pontos", pontosArray);
        if(dataResposta == null || dataResposta.isEmpty()) {
        	resposta.put("action", "listar-pontos");
			resposta.put("error", true);			                
			resposta.put("message", "Falha! Nenhum ponto encontrado");
			resposta.put("data","");
			return resposta;
        }
        resposta.put("action", "listar-pontos");
		resposta.put("error", false);			                
		resposta.put("message", "Sucesso!");
		resposta.put("data", dataResposta);
		return resposta;

	}

	private JSONObject pedirEdicaoPonto(Integer ponto_id) {
		JSONObject resposta = new JSONObject();
		JSONObject ponto = pontos[ponto_id];
		if (ponto == null) {
	        resposta.put("action", "pedido-edicao-ponto");
			resposta.put("error", true);			                
			resposta.put("message", "Falha! Ponto não encontrado");
			resposta.put("data","");
			return resposta;
		}
		JSONObject pontoData = new JSONObject();
		pontoData.put("id", ponto_id);
		pontoData.put("name", ponto.optString("name", ""));
		pontoData.put("obs", ponto.optString("obs", ""));
		JSONObject dataResposta = new JSONObject();
        dataResposta.put("ponto", pontoData);
        
        resposta.put("action", "pedido-edicao-ponto");
		resposta.put("error", false);			                
		resposta.put("message", "Ponto encontrado com sucesso!");
		resposta.put("data", dataResposta);
		return resposta;
	}

	private JSONObject editarPonto(Integer ponto_id, String name, String obs) {
		JSONObject resposta = new JSONObject();
		if (ponto_id == null || ponto_id.toString().isEmpty()) {
			resposta.put("action", "edicao-ponto");
			resposta.put("message", "Falha! 'ponto_id' nulo ou vazio");
			resposta.put("error", true);
			return resposta;
		}
		if (name == null || name.isEmpty()) {
			resposta.put("action", "edicao-ponto");
			resposta.put("message", "Falha! 'name' nulo ou vazio");
			resposta.put("error", true);
			return resposta;
		}		
		if (buscarNomePontoPorId(ponto_id)==null) {
			resposta.put("action", "edicao-ponto");
			resposta.put("message", "Falha! Ponto nao encontrado");
			resposta.put("error", true);
			return resposta;
		}
		JSONObject novo = new JSONObject();
		novo.put("name", name);
		novo.put("obs", obs);
		novo.put("ponto_id", ponto_id);
		pontos[ponto_id] = novo;
		apagarSegmentoPorPonto(ponto_id);
		resposta.put("action", "edicao-ponto");
		resposta.put("message", "Sucesso! Ponto atualizado. Se houver segmento com esse ponto, o mesmo será excluido.");
		resposta.put("error", false);
		return resposta;
	}
        
    // Manter Segmentos//
	
	private Integer encontrarPosicaoVaziaSegmentos() {
		for (int i = 0; i < segmentos.length; i++) {
			if (segmentos[i] == null) {
				return i; 
			}
		}
		return null; 
	}
	
	private JSONObject buscarSegmentoPeloId(Integer segmento_id) {
		JSONObject segmento = segmentos[segmento_id];
		if (segmento != null && segmento_id >= 0) {
			return segmento; 
		}
		return null;
	}
	
	private JSONObject cadastrarSegmento(Integer idPontoOrigem, String namePontoOrigem, String obsPontoOrigem, Integer idPontoDestino , String namePontoDestino, String obsPontoDestino, String direcao, Integer distancia, String obs){
		JSONObject resposta = new JSONObject();
		String action = "cadastro-segmento";
		
		Integer posicao = encontrarPosicaoVaziaSegmentos(); 
		if (posicao == null) {
		resposta.put("action", action);
		resposta.put("error", true);			                
		resposta.put("message", "Erro, espaco insuficiente para adicionar segmento");
		return resposta;
		}
		if ( !isNomePontoCadastrado(namePontoOrigem)) {
			resposta.put("action", action);
			resposta.put("error", true);			                
			resposta.put("message", "Erro, nome do ponto de origem não cadastrado, verifique o nome ou cadastre o ponto desejado");
			return resposta;
		}
				
		if ( !isNomePontoCadastrado(namePontoDestino)) {
			resposta.put("action", action);
			resposta.put("error", true);			                
			resposta.put("message", "Erro, nome do ponto de destino não cadastrado, verifique o nome ou cadastre o ponto desejado");
			return resposta;
		}
		
		JSONObject pontoOrigem = new JSONObject();
		pontoOrigem.put("id", idPontoOrigem);
		pontoOrigem.put("name", namePontoOrigem);
		if(obsPontoOrigem != null) {
			pontoOrigem.put("obs", obsPontoOrigem);
		}
		JSONObject pontoDestino = new JSONObject();
		pontoDestino.put("id", idPontoDestino);
		pontoDestino.put("name", namePontoDestino);
		if(obsPontoDestino != null) {
			pontoDestino.put("obs", obsPontoDestino);
		}
		JSONObject novo = new JSONObject();
		novo.put("id", posicao);
		novo.put("ponto_origem", pontoOrigem);
		novo.put("ponto_destino", pontoDestino);
		novo.put("direcao", direcao);
		novo.put("distancia", distancia);
		if(obs != null) {
			novo.put("obs", obs);
		}
		segmentos[posicao] = novo; 
		
		resposta.put("action", action);
		resposta.put("error", false);			                
		resposta.put("message", "Segmento cadastrado com sucesso");
		return resposta;
	}

	private JSONObject editarSegmento(Integer idSegmento,Integer idPontoOrigem, String namePontoOrigem, String obsPontoOrigem, Integer idPontoDestino , String namePontoDestino, String obsPontoDestino, String direcao, Integer distancia, String obs){
		JSONObject resposta = new JSONObject();
		String action = "edicao-segmento";
		
		Integer posicao = idSegmento; 
		if (buscarSegmentoPeloId(idSegmento) == null) {
		resposta.put("action", action);
		resposta.put("error", true);			                
		resposta.put("message", "Erro, segmento nao encontrado");
		return resposta;
		}
		
		if ( !isNomePontoCadastrado(namePontoOrigem)) {
			resposta.put("action", action);
			resposta.put("error", true);			                
			resposta.put("message", "Erro, nome do ponto de origem não cadastrado, verifique o nome ou cadastre o ponto desejado");
			return resposta;
		}
				
		if ( !isNomePontoCadastrado(namePontoDestino)) {
			resposta.put("action", action);
			resposta.put("error", true);			                
			resposta.put("message", "Erro, nome do ponto de destino não cadastrado, verifique o nome ou cadastre o ponto desejado");
			return resposta;
		}	
		
		JSONObject pontoOrigem = new JSONObject();
		pontoOrigem.put("id", idPontoOrigem);
		pontoOrigem.put("name", namePontoOrigem);
		if(obsPontoOrigem != null) {
			pontoOrigem.put("obs", obsPontoOrigem);
		}
		JSONObject pontoDestino = new JSONObject();
		pontoDestino.put("id", idPontoDestino);
		pontoDestino.put("name", namePontoDestino);
		if(obsPontoDestino != null) {
			pontoDestino.put("obs", obsPontoDestino);
		}
		JSONObject novo = new JSONObject();
		novo.put("id", idSegmento);
		novo.put("ponto_origem", pontoOrigem);
		novo.put("ponto_destino", pontoDestino);
		novo.put("direcao", direcao);
		novo.put("distancia", distancia);
		if(obs != null) {
			novo.put("obs", obs);
		}
		segmentos[posicao] = novo; 
		
		resposta.put("action", action);
		resposta.put("error", false);			                
		resposta.put("message", "Segmento atualizado com sucesso");
		return resposta;
	}
	
	private JSONObject pedirEdicaoSegmento(Integer segmento_id) {
		String action = "pedido-edicao-segmento";
		JSONObject resposta = new JSONObject();
		JSONObject segmento = buscarSegmentoPeloId(segmento_id);
		if (segmento == null) {
	        resposta.put("action", action);
			resposta.put("error", true);			                
			resposta.put("message", "Falha! Segmento não encontrado");
			resposta.put("data","");
			return resposta;
		}
		JSONObject data = new JSONObject();
		data.put("segmento", segmento);
		
        resposta.put("action", action);
		resposta.put("error", false);			                
		resposta.put("message", "Segmento encontrado com sucesso!");
		resposta.put("data", data);
		return resposta;
	}
	
	private JSONObject listarSegmentos() {
		String action = "listar-segmentos" ;
		JSONObject resposta = new JSONObject();
		JSONArray segmentosArray = new JSONArray();   
		for (JSONObject segmento : segmentos) {
			if (segmento != null) {
				segmentosArray.put(segmento);
			}
		}

		JSONObject data = new JSONObject();
       data.put("segmentos", segmentosArray);
       if(data == null || data.isEmpty()) {
        	resposta.put("action", action);
			resposta.put("error", true);			                
			resposta.put("message", "Falha! Nenhum segmento encontrado");
			resposta.put("data","");
			return resposta;
        }
        resposta.put("action", action);
		resposta.put("error", false);			                
		resposta.put("message", "Sucesso!");
		resposta.put("data", data);
		return resposta;

	}
	
	private void apagarSegmentoPorPonto(Integer idPonto) {
		for (int i = 0; i < segmentos.length; i++) {
			JSONObject segmento = segmentos[i];
			if(segmento != null) {
				JSONObject pontoOrigem = segmento.optJSONObject("ponto_origem");
				JSONObject pontoDestino = segmento.optJSONObject("ponto_destino");
				int idPontoOrigem = pontoOrigem.optInt("id");
				int idPontoDestino = pontoDestino.optInt("id");
				if ((idPonto == idPontoOrigem)||idPonto == idPontoDestino)  {
					System.out.println(segmento.toString());
					segmentos[i] = null; 
				}
			}
		}
	}

	private JSONObject excluirSegmento(Integer idSegmento) {
		JSONObject resposta = new JSONObject();
		String action = "excluir-segmento";
		if (buscarSegmentoPeloId(idSegmento) == null) {
			resposta.put("action", action);
			resposta.put("error", true);			                
			resposta.put("message", "Falha! Segmento id:"+idSegmento+ " não encontrado");
			return resposta;
		}
		for (int i = 0; i < segmentos.length; i++) {
			JSONObject segmento = segmentos[i];
			if(segmento != null && idSegmento == segmento.optInt("id")) {
				segmentos[i] = null; 
			}
		}
		resposta.put("action", action);
		resposta.put("error", false);			                
		resposta.put("message", "Segmento id:"+idSegmento+ " excluido com sucesso");
		return resposta;
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
                                if (email == null ||email.isEmpty()) {
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", "login");
                                    resposta.put("error", true);
                                    resposta.put("message", "Informe um e-mail.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                if (senha == null || senha.isEmpty()) {
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", "login");
                                    resposta.put("error", true);
                                    resposta.put("message", "Informe a senha.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (!isValidEmail(email)) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "login");
                                    resposta.put("error", true);
                                    resposta.put("message", "E-mail inválido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                } 
                                if (validarLogin(email, senha)) {
                                	if (buscarTokenPeloEmail(email) != null) {
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "login");
                                        resposta.put("error", true);
                                        resposta.put("message", "Erro! Usuario ja conectado, desconecte da outra sessao.");
                                        out.println(resposta.toString());
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                        break;
                                    }
                                	if (isIpOnUserToken(clientSocket.getInetAddress())) {
                                        JSONObject resposta = new JSONObject();
                                        resposta.put("action", "login");
                                        resposta.put("error", true);
                                        resposta.put("message", "Erro! Ja existe um cliente conectado com esse ip, desconecte o outro cliente.");
                                        out.println(resposta.toString());
                                        log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                        break;
                                    }
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
                                if (token == null || token.isEmpty()) {
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", "logout");
                                    resposta.put("error", true);
                                    resposta.put("message", " O logout falhou! 'token' não enviado ou nulo.");
                                    out.println(resposta.toString());
                                    log("Logout falhou no: "+ clientSocket.getInetAddress());
                                    return;
                                }
                                if (!JwtUtil.isValidToken(token)) {
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", "logout");
                                    resposta.put("error", true);
                                    resposta.put("message", " O logout falhou! Token informado nao e valido.");
                                    out.println(resposta.toString());
                                    log("Logout falhou no: "+ clientSocket.getInetAddress());
                                    return;
                                }
                                
                                if (isTokenOnUserToken(token)) {    
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
                                    resposta.put("message", " O logout falhou! Token informado é diferente do recebido no login.");
                                    out.println(resposta.toString());
                                    log("Logout falhou no: "+ clientSocket.getInetAddress());
                                    return;
                                }
                            }
                            else {
                                JSONObject resposta = new JSONObject();
                                resposta.put("action", "logout");
                                resposta.put("error", true);
                                resposta.put("message", "Campo 'data' nulo ou vazio");
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
                                
                                if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                	JSONObject resposta = new JSONObject();
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (nome == null || email == null || tipo == null || senha == null) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "cadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Campos do json faltando.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }

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
                                    resposta.put("message", "E-mail ja cadastrado para outro usuario.");
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
                                
                                if (nome == null  || email == null || senha == null) {
                                    JSONObject resposta = new JSONObject();
                                    resposta.put("action", "autocadastro-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falha ao cadastrar. Verifique os campos do Json.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);

                                    break;
                                }
                                

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
                                String token = dataIn.optString("token", "");                               
                                JSONObject resposta = new JSONObject();
                                if (token == null || token.isEmpty()) {
                                    resposta.put("action", "listar-usuarios");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falhou ao listar! 'token' não enviado ou nulo.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress() + resposta);
                                    return;
                                }
                                if (!JwtUtil.isValidToken(token)) {
                                    resposta.put("action", "listar-usuarios");
                                    resposta.put("error", true);
                                    resposta.put("message", "Falhou ao listar! Token informado nao e valido.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente "+ clientSocket.getInetAddress() + resposta);
                                    return;
                                }
                                if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
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
                                if (token == null || token.isEmpty()) {
                                    resposta.put("action", "pedido-proprio-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Campo 'token' vazio ou nulo");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                
                                if (!JwtUtil.isValidToken(token)) {
                                    resposta.put("action", "pedido-proprio-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "O token informado nao e validp");
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
                                JSONObject resposta = new JSONObject();
                                if (token == null || token.isEmpty()) {
                                    resposta.put("action", "pedido-proprio-usuario");
                                    resposta.put("error", true);
                                    resposta.put("message", "Campo 'token' vazio ou nulo");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;
                                }
                                
                                if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                    resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                if (!JwtUtil.isValidToken(token)) {
                                	resposta.put("action", action);
                                    resposta.put("error", true);
                                    resposta.put("message", "token invalido");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                    break;
                                }
                                if (usuarioId == null || usuarioId.toString().isEmpty()) { 
                                	resposta.put("error", true);
                                    resposta.put("message", "Falha. Campo 'user_id' nulo ou vazio");
                                    resposta.put("data", "");
                                    out.println(resposta.toString());
                                    log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                    break;                    
                                }
                                
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
							 if (token == null || token.isEmpty()) {
                                 resposta.put("action", "pedido-proprio-usuario");
                                 resposta.put("error", true);
                                 resposta.put("message", "Campo 'token' vazio ou nulo");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": " + resposta);
                                 break;
                             }
                             
                             if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
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

                             if(nome == null || nome.isEmpty()) {
                             	resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Nome não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(email == null || email.isEmpty()) {
                             	resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Email não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(tipo == null || tipo.isEmpty()) {
                             	resposta.put("action", "edicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Tipo de usuário não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if((usuarioId == null) || usuarioId.toString().isEmpty()) {
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
			               if(((!senha.isEmpty()) || senha != null)/*&& (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) == usuarioId)*/) {
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
					        //verifica se o usuario logado é o mesmo que está tendo a senha alterada 

			              /* else if(((!senha.isEmpty()) || senha != null) && (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) != usuarioId)){
			                	resposta.put("action", "edicao-usuario");
					            resposta.put("error", true);
					            resposta.put("message", "Não autorizado! Você pode alterar somente sua própria senha.");
					            out.println(resposta.toString());
					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
				                break;
			                }*/
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
			               if ((isTokenOnUserToken(buscarTokenPeloId(usuarioId)) && (!tipo.equals(cadastros[usuarioId].optString("type","")))) || (isTokenOnUserToken(buscarTokenPeloId(usuarioId)) && (!buscarEmailPeloId(usuarioId).equals(email)))) {
								setOnlyLogoutAllowed(buscarTokenPeloId(usuarioId));
							}
							cadastros[usuarioId].put("name", nome);
							cadastros[usuarioId].put("type", tipo);
							cadastros[usuarioId].put("email", email);
										                
			                resposta.put("action", "edicao-usuario");
			                resposta.put("error", false);
			                resposta.put("message", "Alterações salvas com sucesso.");
			                out.println(resposta.toString());
			                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
			                if (isTokenOnUserToken(token)) {
					        	deleteUserToken(token);
					        	addUserToken(token, clientSocket.getInetAddress());
					        }
			                
			                if(isTokenOnUserToken(buscarTokenPeloId(usuarioId))) {
			                	InetAddress ip = buscarIpPeloId(usuarioId);
			                	deleteUserToken(buscarTokenPeloId(usuarioId));
					        	addUserToken(buscarTokenPeloId(usuarioId), ip);
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
							if (token == null || token.isEmpty()) {
								resposta.put("action", "autoedicao-usuario");
								resposta.put("error", true);
								resposta.put("message", "Chave 'token' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
								
							}
							if (!JwtUtil.isValidToken(token)) {
								resposta.put("action", "autoedicao-usuario");
								resposta.put("error", true);
								resposta.put("message", "token invalido ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
								
							}
							
							if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
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

                             if(nome == null || nome.isEmpty()) {
                             	resposta.put("action", "autoedicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Nome não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(email == null || email.isEmpty()) {
                             	resposta.put("action", "autoedicao-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Email não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if((usuarioId == null )|| usuarioId.toString().isEmpty()) {
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
					        //verifica se o usuario logado é o mesmo que está tendo a senha alterada 
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
			                if ((!senha.isEmpty()) || senha != null) {	
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
			                if (isTokenOnUserToken(token)) {
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
					         JSONObject resposta = new JSONObject();

							 String token = dataIn.optString("token", "");
							 
							 if (token == null || token.isEmpty()) {
									resposta.put("action", "excluir-usuario");
									resposta.put("error", true);
									resposta.put("message", "Chave 'token' esta vazia ou nula ");
									out.println(resposta.toString());
									log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
									break;
									
								}
								if (!JwtUtil.isValidToken(token)) {
									resposta.put("action", "excluir-usuario");
									resposta.put("error", true);
									resposta.put("message", "token invalido ");
									out.println(resposta.toString());
									log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
									break;
									
								}
							 
							 if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
                                 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
							 
                             Integer usuarioId = dataIn.optInt("user_id");
					         
					        if ((usuarioId == null) || usuarioId.toString().isEmpty()) {
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
							 if (token == null || token.isEmpty()) {
								JSONObject resposta = new JSONObject();
								resposta.put("action", "excluir-proprio-usuario");
								resposta.put("error", true);
								resposta.put("message", "Chave 'token' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
									
								}
								if (!JwtUtil.isValidToken(token)) {
									JSONObject resposta = new JSONObject();
									resposta.put("action", "excluir-proprio-usuario");
									resposta.put("error", true);
									resposta.put("message", "token invalido ");
									out.println(resposta.toString());
									log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
									break;
									
								}
							 
							 
							 if(isTokenOnUserToken(token) && isOnlyLogoutAllowed(token)){
                             	JSONObject resposta = new JSONObject();
                                 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
							 
                             String email = dataIn.optString("email", "");
                             String password = dataIn.optString("password", "");
                             Integer usuarioId = buscarId(email);
					         JSONObject resposta = new JSONObject();

                             if(email == null || email.isEmpty()) {
                             	resposta.put("action", "excluir-proprio-usuario");
 					            resposta.put("error", true);
 					            resposta.put("message", "Erro. Email não informado");
 					            out.println(resposta.toString());
 					            log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 				                break;
                             }
                             if(password.isEmpty()) {
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
					        if (!validarLogin(email, password)) {
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
						case "cadastro-ponto":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
							 String name = dataIn.optString("name", "");
                             String obs = dataIn.optString("obs", "");
                             String token = dataIn.optString("token","");
                             if (token == null || token.isEmpty()) {
 								resposta.put("action", action);
 								resposta.put("error", true);
 								resposta.put("message", "Chave 'token' esta vazia ou nula ");
 								out.println(resposta.toString());
 								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 								break;
                             }
                             if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                              }
                             if (name == null || name.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'nome' esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             if (isNomePontoCadastrado(name)) {
                            	resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Erro! Ja existe um ponto com esse nome");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                             }
                             if (obs == null || obs.isEmpty()) {
  								System.out.println("Chave 'obs' do ponto cadastrado esta vazia ou nula ");
                              }
                             resposta = cadastrarPonto(name, obs);
                             out.println(resposta.toString());
 			                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
 			                 break;
						}
						case "pedido-edicao-ponto":{
                            
                            JSONObject resposta = new JSONObject();
                            if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                            Integer ponto_id = dataIn.optInt("ponto_id");
                            String token = dataIn.optString("token","");
                            if (token == null || token.isEmpty()) {
								resposta.put("action", action);
								resposta.put("error", true);
								resposta.put("message", "Chave 'token' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
                            }
                            if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                             }
                            if (!JwtUtil.isUserAdmin(token)) {
                           	 resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha!Usuário sem permissao.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                            }
                            if (ponto_id == null || ponto_id.toString().isEmpty()) {
								resposta.put("action", action);
								resposta.put("error", true);
								resposta.put("message", "Chave 'ponto_id' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
                            }
                            
                            resposta = pedirEdicaoPonto(ponto_id);
                            out.println(resposta);
			                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
			                break;
						}
						case "listar-pontos":{							
                            JSONObject resposta = new JSONObject();
                            if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
							String token = dataIn.optString("token","");

                            if (token == null || token.isEmpty()) {
								resposta.put("action", action);
								resposta.put("error", true);
								resposta.put("message", "Chave 'token' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
                            }
                            if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                             }
                            if(!JwtUtil.isUserAdmin(token)){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! Usuário sem permissao.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                            resposta = listarPontos();
                            out.println(resposta);
			                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
			                break;
						}
						case "edicao-ponto":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
							String token = dataIn.optString("token","");
                            if (token == null || token.isEmpty()) {
								resposta.put("action", action);
								resposta.put("error", true);
								resposta.put("message", "Chave 'token' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
                            }
                            if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                             }
                            if(!JwtUtil.isUserAdmin(token)){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! Usuário sem permissao.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                           String name = dataIn.optString("name","");
                           String obs = dataIn.optString("obs","");
                           Integer ponto_id = dataIn.optInt("ponto_id");
                           resposta = editarPonto(ponto_id, name, obs);
                           out.println(resposta.toString());
                           log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                           break;
						
						}
						case "excluir-ponto":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
							String token = dataIn.optString("token","");
                            if (token == null || token.isEmpty()) {
								resposta.put("action", action);
								resposta.put("error", true);
								resposta.put("message", "Chave 'token' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
                            }
                            if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                             }
                            if(!JwtUtil.isUserAdmin(token)){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! Usuário sem permissao.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                            Integer ponto_id = dataIn.optInt("ponto_id");
                            if (ponto_id == null || ponto_id.toString().isEmpty()) {
								resposta.put("action", action);
								resposta.put("error", true);
								resposta.put("message", "Chave 'ponto_id' esta vazia ou nula ");
								out.println(resposta.toString());
								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
								break;
                            }
                            resposta = excluirPonto(ponto_id);
                            out.println(resposta.toString());
							log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
							break;
						}
						case "edicao-segmento":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                             String token = dataIn.optString("token","");
                             if (token == null || token.isEmpty()) {
 								resposta.put("action", action);
 								resposta.put("error", true);
 								resposta.put("message", "Chave 'token' esta vazia ou nula ");
 								out.println(resposta.toString());
 								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 								break;
                             }
                             if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                              }
                             if (!JwtUtil.isUserAdmin(token)) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Falha!Usuário sem permissao.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
                             JSONObject segmento =  dataIn.optJSONObject("segmento");
                             if (segmento == null || segmento.isEmpty()) {
                             	resposta.put("action", action);
    								resposta.put("error", true);
    								resposta.put("message", "Chave 'segmento' esta vazia ou nula ");
    								out.println(resposta.toString());
    								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
    								break;
                             }                             
                             JSONObject pontoOrigem = segmento.optJSONObject("ponto_origem");
                             if (pontoOrigem == null || pontoOrigem.isEmpty()) {
                            	resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Chave 'ponto_origem' esta vazia ou nula ");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                             }
                             String idPontoOrigemStr = pontoOrigem.optString("id", "");
                             if (idPontoOrigemStr == null || idPontoOrigemStr.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'id' do ponto de origem esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String namePontoOrigem = pontoOrigem.optString("name", "");
                             if (namePontoOrigem == null || namePontoOrigem.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'nome' do ponto de origem esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String obsPontoOrigem = pontoOrigem.optString("obs", "");
                             if (obsPontoOrigem == null || obsPontoOrigem.isEmpty()) {
  								System.out.println("Chave 'obs' do ponto de origem esta vazia ou nula ");
                             }
                             
                             JSONObject pontoDestino = segmento.optJSONObject("ponto_destino");
                             if (pontoDestino == null || pontoDestino.isEmpty()) {
                            	resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Chave 'ponto_destino' esta vazia ou nula ");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                             }
                             String idPontoDestinoStr = pontoDestino.optString("id", "");
                             if (idPontoDestinoStr == null || idPontoDestinoStr.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'id' do ponto de destino esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String namePontoDestino = pontoDestino.optString("name", "");
                             if (namePontoOrigem == null || namePontoOrigem.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'nome' do ponto de destino esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String obsPontoDestino = pontoDestino.optString("obs", "");
                             if (obsPontoDestino == null || obsPontoDestino.isEmpty()) {
  								System.out.println("Chave 'obs' do ponto de destino esta vazia ou nula ");
                             }
                             
                             
                             String direcao = segmento.optString("direcao");
                             if (direcao == null || direcao.isEmpty()) {
    								resposta.put("action", action);
    								resposta.put("error", true);
    								resposta.put("message", "Chave 'direcao' esta vazia ou nula ");
    								out.println(resposta.toString());
    								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
    								break;    
                             }
                             
                             Integer distancia = segmento.optInt("distancia");
                             if (distancia == null || distancia.toString().isEmpty()) {
   								resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Chave 'distancia' esta vazia ou nula ");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                               }
                             
                             String obsSegmento = segmento.optString("obs"); 
                             if (obsSegmento == null || obsSegmento.isEmpty()) {
  								System.out.println("Chave 'obs' do segmento esta vazia ou nula ");
                             }
                             String idSegmentoStr = dataIn.optString("segmento_id");
                             if (idSegmentoStr == null || idSegmentoStr.isEmpty()) {
   								resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Chave 'segmento_id' esta vazia ou nula ");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                               }
                             Integer idSegmento;
                             Integer idPontoOrigem;
                             Integer idPontoDestino;
                             try {
                             	idSegmento = Integer.parseInt(idSegmentoStr);
                             	idPontoOrigem = Integer.parseInt(idPontoOrigemStr);
                             	idPontoDestino = Integer.parseInt(idPontoDestinoStr);
                             } catch (NumberFormatException e) {
                            	resposta.put("action", action);
    							resposta.put("error", true);
    							resposta.put("message", "Chave 'segmento_id' e 'id' deve ser um numero inteiro");
    							out.println(resposta.toString());
    							log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
    							break;
                             }
                             resposta = editarSegmento(idSegmento,idPontoOrigem, namePontoOrigem, obsPontoOrigem, idPontoDestino, namePontoDestino, obsPontoDestino, direcao, distancia, obsSegmento);
                             out.println(resposta.toString());
 			                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
 			                 break;
						}
						case "pedido-edicao-segmento":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                             String token = dataIn.optString("token","");
                             if (token == null || token.isEmpty()) {
 								resposta.put("action", action);
 								resposta.put("error", true);
 								resposta.put("message", "Chave 'token' esta vazia ou nula ");
 								out.println(resposta.toString());
 								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 								break;
                             }
                             if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                             }
                             if (!JwtUtil.isUserAdmin(token)) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Falha!Usuário sem permissao.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
                             String idSegmento = dataIn.optString("segmento_id");
                             if(idSegmento == null || idSegmento.isEmpty()) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Chave 'segmento_id' esta vazia ou nula ");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
                             resposta = pedirEdicaoSegmento(Integer.parseInt(idSegmento));
                             out.println(resposta.toString());
 			                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
 			                 break;
						}
						
						case "listar-segmentos":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                             String token = dataIn.optString("token","");
                             if (token == null || token.isEmpty()) {
 								resposta.put("action", action);
 								resposta.put("error", true);
 								resposta.put("message", "Chave 'token' esta vazia ou nula ");
 								out.println(resposta.toString());
 								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 								break;
                             }
                             if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                             }
                             if (!JwtUtil.isUserAdmin(token)) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Falha!Usuário sem permissao.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
                            
                             resposta = listarSegmentos();
                             out.println(resposta.toString());
 			                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
 			                 break;
						
						}
						case "cadastro-segmento":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                             String token = dataIn.optString("token","");
                             if (token == null || token.isEmpty()) {
 								resposta.put("action", action);
 								resposta.put("error", true);
 								resposta.put("message", "Chave 'token' esta vazia ou nula ");
 								out.println(resposta.toString());
 								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 								break;
                             }
                             if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                              }
                             if (!JwtUtil.isUserAdmin(token)) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Falha!Usuário sem permissao.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
                             JSONObject segmento =  dataIn.optJSONObject("segmento");
                             if (segmento == null || segmento.isEmpty()) {
                             	resposta.put("action", action);
    								resposta.put("error", true);
    								resposta.put("message", "Chave 'segmento' esta vazia ou nula ");
    								out.println(resposta.toString());
    								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
    								break;
                             }                             
                             JSONObject pontoOrigem = segmento.optJSONObject("ponto_origem");
                             if (pontoOrigem == null || pontoOrigem.isEmpty()) {
                            	resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Chave 'ponto_origem' esta vazia ou nula ");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                             }
                             String idPontoOrigem = pontoOrigem.optString("id", "");
                             if (idPontoOrigem == null || idPontoOrigem.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'id' do ponto de origem esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String namePontoOrigem = pontoOrigem.optString("name", "");
                             if (namePontoOrigem == null || namePontoOrigem.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'nome' do ponto de origem esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String obsPontoOrigem = pontoOrigem.optString("obs", "");
                             if (obsPontoOrigem == null || obsPontoOrigem.isEmpty()) {
  								System.out.println("Chave 'obs' do ponto de origem esta vazia ou nula ");
                             }
                             
                             JSONObject pontoDestino = segmento.optJSONObject("ponto_destino");
                             if (pontoDestino == null || pontoDestino.isEmpty()) {
                            	resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Chave 'ponto_destino' esta vazia ou nula ");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                             }
                             String idPontoDestino = pontoDestino.optString("id", "");
                             if (idPontoDestino == null || idPontoDestino.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'id' do ponto de destino esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String namePontoDestino = pontoDestino.optString("name", "");
                             if (namePontoOrigem == null || namePontoOrigem.isEmpty()) {
  								resposta.put("action", action);
  								resposta.put("error", true);
  								resposta.put("message", "Chave 'nome' do ponto de destino esta vazia ou nula ");
  								out.println(resposta.toString());
  								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
  								break;
                              }
                             String obsPontoDestino = pontoDestino.optString("obs", "");
                             if (obsPontoDestino == null || obsPontoDestino.isEmpty()) {
  								System.out.println("Chave 'obs' do ponto de destino esta vazia ou nula ");
                             }
                             
                             
                             String direcao = segmento.optString("direcao");
                             if (direcao == null || direcao.isEmpty()) {
    								resposta.put("action", action);
    								resposta.put("error", true);
    								resposta.put("message", "Chave 'direcao' esta vazia ou nula ");
    								out.println(resposta.toString());
    								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
    								break;    
                             }
                             
                             Integer distancia = segmento.optInt("distancia");
                             if (distancia == null || distancia.toString().isEmpty()) {
   								resposta.put("action", action);
   								resposta.put("error", true);
   								resposta.put("message", "Chave 'distancia' esta vazia ou nula ");
   								out.println(resposta.toString());
   								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
   								break;
                               }
                             
                             String obsSegmento = segmento.optString("obs"); 
                             if (obsSegmento == null || obsSegmento.isEmpty()) {
  								System.out.println("Chave 'obs' do segmento esta vazia ou nula ");
                             }
                             
                             resposta = cadastrarSegmento(Integer.parseInt(idPontoOrigem), namePontoOrigem, obsPontoOrigem, Integer.parseInt(idPontoDestino), namePontoDestino, obsPontoDestino, direcao, distancia, obsSegmento);
                             out.println(resposta.toString());
 			                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
 			                 break;
						}
						
						case "excluir-segmento":{
							JSONObject resposta = new JSONObject();
							if(dataIn == null || dataIn.isEmpty()){
                                resposta.put("action", action);
                                resposta.put("error", true);
                                resposta.put("message", "Falha! 'data' vazio ou nulo.");
                                out.println(resposta.toString());
                                log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                break;
                           }
                             String token = dataIn.optString("token","");
                             if (token == null || token.isEmpty()) {
 								resposta.put("action", action);
 								resposta.put("error", true);
 								resposta.put("message", "Chave 'token' esta vazia ou nula ");
 								out.println(resposta.toString());
 								log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+resposta);
 								break;
                             }
                             if(isOnlyLogoutAllowed(token)){
                                  resposta.put("action", action);
                                  resposta.put("error", true);
                                  resposta.put("message", "Usuário foi desconectado! Faça o logout.");
                                  out.println(resposta.toString());
                                  log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                  break;
                              }
                             if (!JwtUtil.isUserAdmin(token)) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Falha!Usuário sem permissao.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
                             String idSegmentoStr = dataIn.optString("segmento_id");
                             if(idSegmentoStr == null || idSegmentoStr.isEmpty()) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Chave 'segmento_id' esta vazia ou nula ");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
                             }
                             Integer idSegmento;
                             try {
                            	 idSegmento = Integer.parseInt(idSegmentoStr);
                             }
                             catch (NumberFormatException e) {
                            	 resposta.put("action", action);
                                 resposta.put("error", true);
                                 resposta.put("message", "Erro! A chave 'segmento_id' deve ser um numero inteiro.");
                                 out.println(resposta.toString());
                                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress() + ": " + resposta);
                                 break;
							}
                             resposta = excluirSegmento(idSegmento);
                             out.println(resposta.toString());
 			                 log("Servidor->Enviada para o cliente " + clientSocket.getInetAddress()+ ": "+ resposta);
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
                
            } catch (Throwable e) {
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
