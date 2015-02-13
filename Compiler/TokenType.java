/**
 * Created by yangyan on 11/18/14.
 */
public enum TokenType {
    
    T_KEYWORD {
        public String toString() {
            return "Keyword";
        }
    },

    T_SYMBOL {
        public String toString() {
            return "Symbol";
        }
    },

    T_INT {
        public String toString() {
            return "Int_const";
        }
    },

    T_STR {
        public String toString() {
            return "String_const";
        }
    },

    T_ID {
        public String toString() {
            return "Identifier";
        }
    },
}
