package miPaquete;
import java.util.ArrayList;
import java.util.List;

public class ArbolDerivacion {
    private NodoArbol raiz;

    public ArbolDerivacion(List<String> tokens) {
        List<Token> tokensObjetos = convertirTokens(tokens);
        raiz = construirArbol(tokensObjetos);
    }

    private List<Token> convertirTokens(List<String> tokens) {
        List<Token> tokensObjetos = new ArrayList<>();
        for (String token : tokens) {
            String[] partes = token.split(": ");
            TipoToken tipo = TipoToken.valueOf(partes[0]);
            String valor = partes[1];
            tokensObjetos.add(new Token(tipo, valor));
        }
        return tokensObjetos;
    }

    private NodoArbol construirArbol(List<Token> tokens) {
        NodoArbol nodo = new NodoArbol("<programa>");

        List<Token> lineaActual = new ArrayList<>();
        for (Token token : tokens) {
            if (tokens.isEmpty()) {  // Caso base: lista vacía, regresa un nodo terminal
                return new NodoArbol("<vacio>");
            }
            else {
                NodoArbol nodoError = new NodoArbol("<error>");
                nodoError.agregarHijo(new NodoArbol(token.valor));
                nodo.agregarHijo(nodoError);
                nodo.agregarHijo(construirArbol(lineaActual));
                lineaActual.clear();
            }
        }
        nodo.agregarHijo(construirArbol(lineaActual));

        return nodo;
    }

    public void imprimirArbol() {
        raiz.imprimir("");
    }

    private static class NodoArbol {
        private String valor;
        private List<NodoArbol> hijos;

        public NodoArbol(String valor) {
            this.valor = valor;
            this.hijos = new ArrayList<>();
        }

        public void agregarHijo(NodoArbol hijo) {
            hijos.add(hijo);
        }

        public void imprimir(String prefijo) {
            System.out.println(prefijo + valor);
            for (NodoArbol hijo : hijos) {
                hijo.imprimir(prefijo + "  ");
            }
        }
    }

    private static class Token {
        private final TipoToken tipo;
        private final String valor;

        public Token(TipoToken tipo, String valor) {
            this.tipo = tipo;
            this.valor = valor;
        }
    }

    private enum TipoToken {
        PalabraReservada, COMBINACION, Constante, OPERADOR, ID, Cadena, ERROR
    }
}
