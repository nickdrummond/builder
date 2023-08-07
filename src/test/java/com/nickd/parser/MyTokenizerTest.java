package com.nickd.parser;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MyTokenizerTest {

    @Test
    public void shouldTokenizeUsingSpaces() {
        MyTokenizer t = new MyTokenizer("a b c");
        assertEquals("a", t.consumeNext());
        assertEquals("b", t.consumeNext());
        assertEquals("c", t.consumeNext());
        assertEquals(List.of("c", "b", "a"), t.tokens());
        assertEquals(5, t.getPointer());
    }

    @Test
    public void shouldHandleLeadingSpace() {
        MyTokenizer t = new MyTokenizer("    a b c");
        assertEquals("a", t.consumeNext());
        assertEquals("b", t.consumeNext());
        assertEquals("c", t.consumeNext());
        assertEquals(List.of("c", "b", "a"), t.tokens());
        assertEquals(9, t.getPointer());
    }


    @Test
    public void shouldHandleTrailingSpace() {
        MyTokenizer t = new MyTokenizer("a b c    ");
        assertEquals("a", t.consumeNext());
        assertEquals("b", t.consumeNext());
        assertEquals("c", t.consumeNext());
        assertEquals(List.of("c", "b", "a"), t.tokens());
        assertEquals(9, t.getPointer());
    }

    @Test
    public void shouldHandleRandomSpace() {
        MyTokenizer t = new MyTokenizer("a    b  c");
        assertEquals("a", t.consumeNext());
        assertEquals("b", t.consumeNext());
        assertEquals("c", t.consumeNext());
        assertEquals(List.of("c", "b", "a"), t.tokens());
        assertEquals(9, t.getPointer());
    }


    @Test
    public void shouldHandleDifferentLengthTokens() {
        MyTokenizer t = new MyTokenizer("ABBA beegees corbyn");
        assertEquals("ABBA", t.consumeNext());
        assertEquals("beegees", t.consumeNext());
        assertEquals("corbyn", t.consumeNext());
        assertEquals(List.of("corbyn", "beegees", "ABBA"), t.tokens());
        assertEquals(19, t.getPointer());
    }


    @Test
    public void shouldTokenizeParenthesis() {
        MyTokenizer t = new MyTokenizer("ABBA ( beegees ) corbyn");
        assertEquals("ABBA", t.consumeNext());
        assertEquals("(", t.consumeNext());
        assertEquals("beegees", t.consumeNext());
        assertEquals(")", t.consumeNext());
        assertEquals("corbyn", t.consumeNext());
        assertEquals(List.of("corbyn", ")", "beegees", "(", "ABBA"), t.tokens());
        assertEquals(23, t.getPointer());
    }

    @Test
    public void shouldTokenizeParenthesisDirectlyAroundTokens() {
        MyTokenizer t = new MyTokenizer("ABBA (beegees corbyn)");
        assertEquals("ABBA", t.consumeNext());
        assertEquals("(", t.consumeNext());
        assertEquals("beegees", t.consumeNext());
        assertEquals("corbyn", t.consumeNext());
        assertEquals(")", t.consumeNext());
        assertEquals(List.of(")", "corbyn", "beegees", "(", "ABBA"), t.tokens());
        assertEquals(21, t.getPointer());
    }

    @Test
    public void shouldTokenizeQuotedTextCorrectly() {
        MyTokenizer t = new MyTokenizer("this is \"five tokens\" not six");
        assertEquals("this", t.consumeNext());
        assertEquals("is", t.consumeNext());
        assertEquals("\"five tokens\"", t.consumeNext());
        assertEquals("not", t.consumeNext());
        assertEquals("six", t.consumeNext());
    }

    @Test
    public void shouldTokenizeLangStyleTokensCorrectly() {
        MyTokenizer t = new MyTokenizer("a \"label with lang\"@en and more");
        assertEquals("a", t.consumeNext());
        assertEquals("\"label with lang\"", t.consumeNext());
        assertEquals("@en", t.consumeNext());
        assertEquals("and", t.consumeNext());
        assertEquals("more", t.consumeNext());
    }

    @Test
    public void shouldTokenizeDatatypeStyleTokensCorrectly() {
        MyTokenizer t = new MyTokenizer("a \"label with dt\"^^anyURI and more");
        assertEquals("a", t.consumeNext());
        assertEquals("\"label with dt\"", t.consumeNext());
        assertEquals("^^anyURI", t.consumeNext());
        assertEquals("and", t.consumeNext());
        assertEquals("more", t.consumeNext());
    }
}