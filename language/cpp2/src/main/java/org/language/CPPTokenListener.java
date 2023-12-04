package org.language;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.language_api.TokenType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.language.CPP14Parser.RULE_selectionStatement;
import static org.language.CPPTokenType.*;

public class CPPTokenListener  extends CPP14ParserBaseListener{
    private final CPPParserAdapter parser;
    private final Deque<TokenType> trackedState = new ArrayDeque<>();
    private Token lastElseToken;

    /**
     * Constructs a new token listener that will extract tokens to the given {@link CPPParserAdapter}.
     * @param parser the adapter to pass extracted tokens to.
     */
    public CPPTokenListener(CPPParserAdapter parser) {
        this.parser = parser;
    }

    private static final List<Extraction<CPP14Parser.ClassSpecifierContext>> CLASS_SPECIFIER_TOKENS = List.of(
            Extraction.of(context -> context.classHead().Union(), UNION_BEGIN, UNION_END),
            Extraction.of(context -> context.classHead().classKey().Class(), CLASS_BEGIN, CLASS_END),
            Extraction.of(context -> context.classHead().classKey().Struct(), STRUCT_BEGIN, STRUCT_END));

    @Override
    public void enterClassSpecifier(CPP14Parser.ClassSpecifierContext context) {
        extractFirstNonNullStartToken(context, context.getStart(), CLASS_SPECIFIER_TOKENS);
    }

    @Override
    public void exitClassSpecifier(CPP14Parser.ClassSpecifierContext context) {
        extractFirstNonNullEndToken(context, context.getStop(), CLASS_SPECIFIER_TOKENS);
    }

    @Override
    public void enterEnumSpecifier(CPP14Parser.EnumSpecifierContext context) {
        addEnter(ENUM_BEGIN, context.getStart());
    }

    @Override
    public void exitEnumSpecifier(CPP14Parser.EnumSpecifierContext context) {
        addExit(ENUM_END, context.getStop());
    }

    @Override
    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext context) {
        addEnter(FUNCTION_BEGIN, context.getStart());
    }

    @Override
    public void exitFunctionDefinition(CPP14Parser.FunctionDefinitionContext context) {
        addExit(FUNCTION_END, context.getStop());
    }

    private static final List<Extraction<CPP14Parser.IterationStatementContext>> ITERATION_STATEMENT_TOKENS = List.of(
            Extraction.of(CPP14Parser.IterationStatementContext::Do, DO_BEGIN, DO_END), Extraction.of(CPP14Parser.IterationStatementContext::For, FOR_BEGIN, FOR_END),
            Extraction.of(CPP14Parser.IterationStatementContext::While, WHILE_BEGIN, WHILE_END));

    @Override
    public void enterIterationStatement(CPP14Parser.IterationStatementContext context) {
        extractFirstNonNullStartToken(context, context.getStart(), ITERATION_STATEMENT_TOKENS);
    }

    @Override
    public void exitIterationStatement(CPP14Parser.IterationStatementContext context) {
        extractFirstNonNullEndToken(context, context.getStop(), ITERATION_STATEMENT_TOKENS);
    }

    @Override
    public void enterSelectionStatement(CPP14Parser.SelectionStatementContext context) {
        if (context.Switch() != null) {
            addEnter(SWITCH_BEGIN, context.getStart());
            this.trackedState.add(CPPTokenType.SWITCH_END);
        } else if (context.If() != null) {
            addEnter(IF_BEGIN, context.getStart());
            if (context.Else() != null) {
                this.trackedState.add(ELSE);
                this.lastElseToken = context.Else().getSymbol();
            }
            this.trackedState.add(CPPTokenType.IF_END);
        }
    }

    @Override
    public void enterStatement(CPP14Parser.StatementContext context) {
        if (context.getParent().getRuleIndex() == RULE_selectionStatement
                && this.trackedState.peekLast() == CPPTokenType.ELSE) {
            addEnter(trackedState.removeLast(), this.lastElseToken);
        }
    }

    @Override
    public void exitStatement(CPP14Parser.StatementContext context) {
        if (context.getParent().getRuleIndex() == RULE_selectionStatement
                && this.trackedState.peekLast() == CPPTokenType.IF_END) {

            trackedState.removeLast();
        }
    }

    @Override
    public void exitSelectionStatement(CPP14Parser.SelectionStatementContext context) {
        if (context.Switch() != null) {
            addEnter(SWITCH_END, context.getStop());
        } else if (context.If() != null) {
            addEnter(IF_END, context.getStop());
        }
    }

    private static final List<Extraction<CPP14Parser.LabeledStatementContext>> LABELED_STATEMENT_TOKES = List
            .of(Extraction.of(CPP14Parser.LabeledStatementContext::Case, CASE),
                    Extraction.of(CPP14Parser.LabeledStatementContext::Default, DEFAULT));

    @Override
    public void enterLabeledStatement(CPP14Parser.LabeledStatementContext context) {
        extractFirstNonNullStartToken(context, context.start, LABELED_STATEMENT_TOKES);
    }

    @Override
    public void enterTryBlock(CPP14Parser.TryBlockContext context) {
        addEnter(TRY, context.getStart());
    }

    @Override
    public void enterHandler(CPP14Parser.HandlerContext context) {
        addEnter(CATCH_BEGIN, context.getStart());
    }

    @Override
    public void exitHandler(CPP14Parser.HandlerContext context) {
        addEnter(CATCH_END, context.getStop());
    }

    private static final List<Extraction<CPP14Parser.JumpStatementContext>> JUMP_STATEMENT_TOKENS = List.of
            (Extraction.of(CPP14Parser.JumpStatementContext::Break, BREAK),
            Extraction.of(CPP14Parser.JumpStatementContext::Continue, CONTINUE), Extraction.of(CPP14Parser.JumpStatementContext::Goto, GOTO),
            Extraction.of(CPP14Parser.JumpStatementContext::Return, RETURN));

    @Override
    public void enterJumpStatement(CPP14Parser.JumpStatementContext context) {
        extractFirstNonNullStartToken(context, context.getStart(), JUMP_STATEMENT_TOKENS);
    }

    @Override
    public void enterThrowExpression(CPP14Parser.ThrowExpressionContext context) {
        addEnter(THROW, context.getStart());
    }

    private static final List<Extraction<CPP14Parser.NewExpressionContext>> NEW_EXPRESSION_TOKENS = List
            .of(Extraction.of(CPP14Parser.NewExpressionContext::newInitializer, NEWCLASS), Extraction.fallback(NEWARRAY));

    @Override
    public void enterNewExpression(CPP14Parser.NewExpressionContext context) {
        extractFirstNonNullStartToken(context, context.getStart(), NEW_EXPRESSION_TOKENS);
    }

    @Override
    public void enterTemplateDeclaration(CPP14Parser.TemplateDeclarationContext context) {
        addEnter(GENERIC, context.getStart());
    }

    @Override
    public void enterAssignmentOperator(CPP14Parser.AssignmentOperatorContext context) {
        addEnter(ASSIGN, context.getStart());
    }

    @Override
    public void enterBraceOrEqualInitializer(CPP14Parser.BraceOrEqualInitializerContext context) {
        if (context.Assign() != null) {
            addEnter(ASSIGN, context.getStart());
        }
    }

    @Override
    public void enterUnaryExpression(CPP14Parser.UnaryExpressionContext context) {
        if (context.PlusPlus() != null || context.MinusMinus() != null) {
            addEnter(ASSIGN, context.getStart());
        }
    }

    @Override
    public void enterStaticAssertDeclaration(CPP14Parser.StaticAssertDeclarationContext context) {
        addEnter(STATIC_ASSERT, context.getStart());
    }

    @Override
    public void enterEnumeratorDefinition(CPP14Parser.EnumeratorDefinitionContext context) {
        addEnter(VARDEF, context.getStart());
    }

    @Override
    public void enterBracedInitList(CPP14Parser.BracedInitListContext context) {
        addEnter(BRACED_INIT_BEGIN, context.getStart());
    }

    @Override
    public void exitBracedInitList(CPP14Parser.BracedInitListContext context) {
        addExit(BRACED_INIT_END, context.getStop());
    }

    @Override
    public void enterSimpleTypeSpecifier(CPP14Parser.SimpleTypeSpecifierContext context) {
        if (hasAncestor(context, CPP14Parser.MemberdeclarationContext.class, CPP14Parser.FunctionDefinitionContext.class)) {
            addEnter(VARDEF, context.getStart());
        } else if (hasAncestor(context, CPP14Parser.SimpleDeclarationContext.class, CPP14Parser.TemplateArgumentContext.class, CPP14Parser.FunctionDefinitionContext.class)) {

            CPP14Parser.SimpleDeclarationContext parent = getAncestor(context, CPP14Parser.SimpleDeclarationContext.class);
            assert parent != null; // already checked by hasAncestor
            CPP14Parser.NoPointerDeclaratorContext noPointerDecl = getDescendant(parent, CPP14Parser.NoPointerDeclaratorContext.class);
            if ((!noPointerInFunctionCallContext(noPointerDecl)) && !hasAncestor(context, CPP14Parser.NewTypeIdContext.class)) {

                addEnter(VARDEF, context.getStart());
            }
        }
    }

    @Override
    public void enterSimpleDeclaration(CPP14Parser.SimpleDeclarationContext context) {
        if (!hasAncestor(context, CPP14Parser.FunctionBodyContext.class)) {
            // not in a context where a function call can appear, assume it's a function definition
            return;
        }
        CPP14Parser.NoPointerDeclaratorContext noPointerDecl = getDescendant(context, CPP14Parser.NoPointerDeclaratorContext.class);
        if (noPointerInFunctionCallContext(noPointerDecl)) {
            // method calls like A::b(), b()
            addEnter(APPLY, noPointerDecl.getStart());
        }
    }

    /**
     * {@return true of this context represents a function call}
     */
    private static boolean noPointerInFunctionCallContext(CPP14Parser.NoPointerDeclaratorContext context) {
        return context != null && (context.parametersAndQualifiers() != null || context.LeftParen() != null);
    }

    @Override
    public void enterParameterDeclaration(CPP14Parser.ParameterDeclarationContext context) {
        addEnter(VARDEF, context.getStart());
    }

    @Override
    public void enterConditionalExpression(CPP14Parser.ConditionalExpressionContext context) {
        if (context.Question() != null) {
            addEnter(QUESTIONMARK, context.getStart());
        }
    }

    private static final List<Extraction<CPP14Parser.PostfixExpressionContext>> POSTFIX_EXPRESSION_TOKENS = List.of(
            Extraction.of(CPP14Parser.PostfixExpressionContext::LeftParen, APPLY),
            Extraction.of(CPP14Parser.PostfixExpressionContext::PlusPlus, ASSIGN),
            Extraction.of(CPP14Parser.PostfixExpressionContext::MinusMinus, ASSIGN));

    @Override
    public void enterPostfixExpression(CPP14Parser.PostfixExpressionContext context) {

        extractFirstNonNullStartToken(context, context.getStart(), POSTFIX_EXPRESSION_TOKENS);
    }

    private <T extends ParserRuleContext> T getDescendant(ParserRuleContext context, Class<T> descendant) {
        // simple iterative bfs
        ArrayDeque<ParserRuleContext> queue = new ArrayDeque<>();
        queue.add(context);
        while (!queue.isEmpty()) {
            ParserRuleContext next = queue.removeFirst();
            for (ParseTree tree : next.children) {
                if (tree.getClass() == descendant) {
                    return descendant.cast(tree);
                }
                if (tree instanceof ParserRuleContext parserRuleContext) {
                    queue.addLast(parserRuleContext);
                }
            }
        }
        return null;
    }
    @SafeVarargs
    private <T extends ParserRuleContext> T getAncestor(ParserRuleContext context, Class<T> ancestor, Class<? extends ParserRuleContext>... stops) {
        ParserRuleContext currentcontext = context;
        Set<Class<? extends ParserRuleContext>> forbidden = Set.of(stops);
        do {
            ParserRuleContext next = currentcontext.getParent();
            if (next == null) {
                return null;
            }
            if (next.getClass() == ancestor) {
                return ancestor.cast(next);
            }
            if (forbidden.contains(next.getClass())) {
                return null;
            }
            currentcontext = next;
        } while (true);
    }
    @SafeVarargs
    private boolean hasAncestor(ParserRuleContext context, Class<? extends ParserRuleContext> parent, Class<? extends ParserRuleContext>... stops) {
        return getAncestor(context, parent, stops) != null;
    }

    // extraction utilities

    private void addEnter(TokenType type, Token token) {
        addTokenWithLength(type, token, token.getText().length());
    }

    private void addExit(TokenType type, Token token) {
        addTokenWithLength(type, token, 1);
    }

    private void addTokenWithLength(TokenType type, Token token, int length) {
        int column = token.getCharPositionInLine() + 1;
        this.parser.addToken(type, column, token.getLine(), length);
    }


    private record Extraction<T>(Predicate<T> extractionTest, TokenType startToken, TokenType endToken) {


        static <T> Extraction<T> of(Function<T, ?> contextToAnything, TokenType startToken) {
            return of(contextToAnything, startToken, null);
        }

        static <T> Extraction<T> of(Function<T, ?> contextToAnything, TokenType startToken, TokenType endToken) {

            Predicate<T> isNonNull = t -> contextToAnything.andThen(Objects::nonNull).apply(t);
            return new Extraction<>(isNonNull, startToken, endToken);
        }

        static <T> Extraction<T> fallback(TokenType toExtract) {
            return new Extraction<>(t -> true, toExtract, null);
        }
    }

    private <T> void extractFirstNonNullEndToken(T context, Token token, List<Extraction<T>> extractions) {
        extractFirstNonNull(context, token, extractions, false);
    }

    private <T> void extractFirstNonNullStartToken(T context, Token token, List<Extraction<T>> extractions) {
        extractFirstNonNull(context, token, extractions, true);
    }

    private <T> void extractFirstNonNull(T context, Token token, List<Extraction<T>> extractions, boolean start) {
        for (Extraction<? super T> extraction : extractions) {
            if (extraction.extractionTest().test(context)) {
                if (start) {
                    addEnter(extraction.startToken(), token);
                } else {
                    addExit(extraction.endToken(), token);
                }
                return;
            }
        }
    }

}
