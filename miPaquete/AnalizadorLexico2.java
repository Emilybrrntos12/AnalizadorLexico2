package miPaquete;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import miPaquete.ArbolDerivacion;

public class AnalizadorLexico2 {
    private static final List<String> palabrasReservadas = List.of("if", "else", "for", "print", "int", "string");
    private static final List<String> combinacionesPosibles = List.of("bfhjk", "ifbfhjk", "elsebfhjk", "forbfhjk", "printbfhjk", "intbfhjk");
    private static final Pattern identi = Pattern.compile("^[a-zA-Z][a-zA-Z0-9.]*$"); //empieza con mayuscula o minuscula y puede contener letras y numeros
    private static final Pattern pconstante = Pattern.compile("^[0-9]+$"); //expresion regular para constante numericas 
    private static final Pattern poperador = Pattern.compile("^(\\+|-|\\*|/|:=|>=|<=|>|<|=|<>|\\{|\\}|\\[|\\]|\\(|\\)|,|;)$"); //operadores del lenguaje, e incluye los de 
    //operaciones artemeticas

    public static void main(String[] args) {
        List<Token> tokens = new ArrayList<>();
        List<ErrorLexico> errores = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\emibr\\OneDrive\\Desktop\\codigo_fuente.txt"))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = eliminarComentarios(linea);
                List<Token> tokensLinea = tokenizar(linea);
                tokens.addAll(tokensLinea);
                errores.addAll(filtrarErrores(tokensLinea));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println("\nErrores léxicos:");
        for (ErrorLexico error : errores) {
            System.out.println(error);
        }

       // Convertir la lista de Token a una lista de String
        List<String> tokensString = new ArrayList<>();
        for (Token token : tokens) {
            tokensString.add(token.tipo + ": " + token.valor);
        }

        // Construir el árbol de derivación
        ArbolDerivacion arbol = new ArbolDerivacion(tokensString);
        System.out.println("\nÁrbol de derivación:");
        arbol.imprimirArbol();
    }

    //eliminacion de los comanetarios
    private static String eliminarComentarios(String linea)
    {
        int inicioComentario = linea.indexOf("//"); //busca la posicion del inicio del comentario
        if (inicioComentario != -1) {
            linea = linea.substring(0, inicioComentario); //encontrado el comentario, se actualiza la variable linea
            //para comentar soo texto antes del //
        }
        return linea; //ya devuelve la linea sin comentario los elimina
    }

    //devuelve la lista de tokens
    private static List<Token> tokenizar(String linea) {
        List<Token> tokens = new ArrayList<>();
        int indice = 0;
        while (indice < linea.length()) /*aca da el inicio para recorrer 
        la liena de entrada caracter por caracter*/
        {
            char c = linea.charAt(indice); //posicion actual del inidice
            if (Character.isWhitespace(c))  /*verifica si el caracter es un espacio en blanco
            y lo ifnora*/
            {
                indice++;
                continue;
            }
            //para las cadenas
            if (c == '"') {
                StringBuilder cadena = new StringBuilder(); //para construir la cadena entre comillas
                cadena.append(c);
                indice++;
                while (indice < linea.length() && linea.charAt(indice) != '"')
                //bucle para contruir la cadena hasta su cierre de comillas
                {
                    cadena.append(linea.charAt(indice));
                    indice++;
                }
                if (indice < linea.length() && linea.charAt(indice) == '"')
                 /*verifica si se encontro el cierre de comillas y lo agrega al token cadena*/
                {
                    cadena.append('"');
                    indice++;
                    tokens.add(new Token(TipoToken.Cadena, cadena.toString()));
                } else {
                    tokens.add(new Token(TipoToken.ERROR, cadena.toString()));
                }
                continue;
            }
            if (c == '(' || c == ')' || c == '{' || c == '}' || c == ';') {
                tokens.add(new Token(TipoToken.OPERADOR, String.valueOf(c)));
                indice++;
                continue;
            }
            StringBuilder buffer = new StringBuilder(); //para construir otros tipos de tokens
            buffer.append(c);
            indice++; //avanza al sig caracter
            boolean encontradoSiguienteToken = false; /*controla si se ha encontrado el fianl del 
            token actual*/
            while (indice < linea.length() && !encontradoSiguienteToken) /*tokens que no son cadenas 
            ni operadores*/
            {
                char siguiente = linea.charAt(indice);
                if (Character.isWhitespace(siguiente) || poperador.matcher(String.valueOf(siguiente)).matches() || siguiente == '"' || siguiente == '(' || siguiente == ')' || siguiente == '{' || siguiente == '}' || siguiente == ';') {
                    encontradoSiguienteToken = true;
                } else {
                    buffer.append(siguiente);
                    indice++;
                }
            }
            String valor = buffer.toString();
            if (palabrasReservadas.contains(valor)) {
                tokens.add(new Token(TipoToken.PalabraReservada, valor)); /*se agrega a la lista de tokens*/
            } else if (combinacionesPosibles.contains(valor)) {
                tokens.add(new Token(TipoToken.COMBINACION, valor));
            } else if (pconstante.matcher(valor).matches()) {
                tokens.add(new Token(TipoToken.Constante, valor));
            } else if (poperador.matcher(valor).matches()) {
                tokens.add(new Token(TipoToken.OPERADOR, valor));
            } else if (identi.matcher(valor).matches()) {
                tokens.add(new Token(TipoToken.ID, valor));
            } else {
                tokens.add(new Token(TipoToken.ERROR, valor));
            }
        }
        return tokens;
    }

    private static List<ErrorLexico> filtrarErrores(List<Token> tokens) 
    {
        List<ErrorLexico> errores = new ArrayList<>(); //se van a almacenar los errores lexicos
        for (Token token : tokens) {
            if (token.tipo == TipoToken.ERROR) {
                errores.add(new ErrorLexico(token.valor, TipoError.VALOR_INVALIDO));
                /*agrega un nuevo objeto a la lista de errores, utilizando el valor del token como descripción 
                del error y asignando un tipo de error VALOR_INVALIDO.*/
            }
        }
        return errores;
    }

    //enumerado de tokens
    private enum TipoToken {
        PalabraReservada, COMBINACION, Constante, OPERADOR, ID, Cadena, ERROR
    }

    //enumeracion de los erroes
    private enum TipoError {
        VALOR_INVALIDO
    }

    //clase token del analizador
    private static class Token 
    {
        private final TipoToken tipo; //declaracion de campo
        private final String valor;

        public Token(TipoToken tipo, String valor) {
            this.tipo = tipo;
            this.valor = valor;
        }

        //por si hay una cadena
        @Override
        public String toString() {
            return tipo + ": " + valor;
        }
    }

    //clase de los errores
    private static class ErrorLexico {
        private final String valor; //va almacenar la descripcion del error
        private final TipoError tipo; //representa ell tipo de error lexico

        public ErrorLexico(String valor, TipoError tipo) {
            this.valor = valor;
            this.tipo = tipo;
        }

        @Override
        public String toString() {
            return "Error: " + valor + " (" + tipo + ")";
        }
    }
}
