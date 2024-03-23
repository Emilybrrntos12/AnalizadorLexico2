import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AnalizadorLexico2 {
    private static final List<String> palabrasReservadas = List.of("if", "else", "for", "print", "int", "string");
    private static final List<String> combinacionesPosibles = List.of("bfhjk", "ifbfhjk", "elsebfhjk", "forbfhjk", "printbfhjk", "intbfhjk");
    private static final Pattern identi = Pattern.compile("^[a-zA-Z][a-zA-Z0-9.]*$");
    private static final Pattern pconstante = Pattern.compile("^[0-9]+$");
    private static final Pattern poperador = Pattern.compile("^(\\+|-|\\*|/|:=|>=|<=|>|<|=|<>|\\{|\\}|\\[|\\]|\\(|\\)|,|;)$");

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
    }

    private static String eliminarComentarios(String linea) {
        int inicioComentario = linea.indexOf("//");
        if (inicioComentario != -1) {
            linea = linea.substring(0, inicioComentario);
        }
        return linea;
    }

    private static List<Token> tokenizar(String linea) {
        List<Token> tokens = new ArrayList<>();
        int indice = 0;
        while (indice < linea.length()) {
            char c = linea.charAt(indice);
            if (Character.isWhitespace(c)) {
                indice++;
                continue;
            }
            if (c == '"') {
                StringBuilder cadena = new StringBuilder();
                cadena.append(c);
                indice++;
                while (indice < linea.length() && linea.charAt(indice) != '"') {
                    cadena.append(linea.charAt(indice));
                    indice++;
                }
                if (indice < linea.length() && linea.charAt(indice) == '"') {
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
            StringBuilder buffer = new StringBuilder();
            buffer.append(c);
            indice++;
            boolean encontradoSiguienteToken = false;
            while (indice < linea.length() && !encontradoSiguienteToken) {
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
                tokens.add(new Token(TipoToken.PalabraReservada, valor));
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

    private static List<ErrorLexico> filtrarErrores(List<Token> tokens) {
        List<ErrorLexico> errores = new ArrayList<>();
        for (Token token : tokens) {
            if (token.tipo == TipoToken.ERROR) {
                errores.add(new ErrorLexico(token.valor, TipoError.VALOR_INVALIDO));
            }
        }
        return errores;
    }

    private enum TipoToken {
        PalabraReservada, COMBINACION, Constante, OPERADOR, ID, Cadena, ERROR
    }

    private enum TipoError {
        VALOR_INVALIDO
    }

    private static class Token {
        private final TipoToken tipo;
        private final String valor;

        public Token(TipoToken tipo, String valor) {
            this.tipo = tipo;
            this.valor = valor;
        }

        @Override
        public String toString() {
            return tipo + ": " + valor;
        }
    }

    private static class ErrorLexico {
        private final String valor;
        private final TipoError tipo;

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
