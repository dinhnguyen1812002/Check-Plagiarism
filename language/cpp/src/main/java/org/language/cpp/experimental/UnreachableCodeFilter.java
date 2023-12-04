package org.language.cpp.experimental;

import org.language_api.Token;
import org.language_api.TokenType;

import java.util.List;
import java.util.ListIterator;

import static org.language.cpp.CPPTokenType.*;
import static org.language_api.SharedTokenType.FILE_END;

public final class UnreachableCodeFilter {

    private UnreachableCodeFilter() {
    }

    public static void applyTo(List<Token> tokenList) {
        TokenFilterState stateMachine = TokenFilterState.STATE_DEFAULT;

        ListIterator<Token> iterator = tokenList.listIterator();
        while (iterator.hasNext()) {
            var token = iterator.next();

            stateMachine = stateMachine.nextState(token.getType());

            if (stateMachine.shouldTokenBeDeleted()) {
                iterator.remove();
            }
        }
    }

    private enum TokenFilterState {
        STATE_DEFAULT {
            @Override
            TokenFilterState nextState(TokenType nextType) {
                if (isBlockStartToken(nextType)) {
                    return STATE_BLOCK_BEGINNING;
                }
                if (isJumpToken(nextType)) {
                    return STATE_DEAD_BLOCK_BEGINNING;
                }
                if (nextType == C_CASE) {
                    return STATE_CASE_BLOCK;
                }
                return STATE_DEFAULT;
            }
        },
        STATE_BLOCK_BEGINNING {
            @Override
            TokenFilterState nextState(TokenType nextType) {
                if (isBlockEndToken(nextType) || nextType == C_BLOCK_BEGIN) {
                    return STATE_DEFAULT;
                }
                return STATE_BLOCK_BEGINNING;
            }
        },
        STATE_DEAD_BLOCK {
            @Override
            TokenFilterState nextState(TokenType nextType) {
                if (isBlockEndToken(nextType)) {
                    return STATE_DEFAULT;
                }
                if (nextType == C_CASE) {
                    return STATE_CASE_BLOCK;
                }
                return STATE_DEAD_BLOCK;
            }

            @Override
            public boolean shouldTokenBeDeleted() {
                return true;
            }
        },

        STATE_DEAD_BLOCK_BEGINNING {
            @Override
            TokenFilterState nextState(TokenType nextType) {
                if (isBlockEndToken(nextType)) {
                    return STATE_DEFAULT;
                }
                if (nextType == C_CASE) {
                    return STATE_CASE_BLOCK;
                }
                return STATE_DEAD_BLOCK;
            }
        },

        STATE_CASE_BLOCK {
            @Override
            TokenFilterState nextState(TokenType nextType) {
                if (isBlockEndToken(nextType)) {
                    return STATE_DEFAULT;
                }
                if (isJumpToken(nextType)) {
                    return STATE_DEAD_BLOCK_BEGINNING;
                }
                return STATE_CASE_BLOCK;
            }
        };

        private static boolean isBlockStartToken(TokenType token) {
            return token == C_WHILE || token == C_IF || token == C_FOR;
        }

        private static boolean isBlockEndToken(TokenType token) {
            return token == C_BLOCK_END || token == FILE_END;
        }


        private static boolean isJumpToken(TokenType token) {
            return token == C_RETURN || token == C_BREAK || token == C_CONTINUE || token == C_THROW || token == C_GOTO;
        }


        public boolean shouldTokenBeDeleted() {
            return false;
        }

        abstract TokenFilterState nextState(TokenType nextType);
    }
}

