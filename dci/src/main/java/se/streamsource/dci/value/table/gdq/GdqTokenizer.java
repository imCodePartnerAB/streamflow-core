package se.streamsource.dci.value.table.gdq;

class GdqTokenizer {

   String s;
   int nextTokenPosition;
   GdqTokenType currentTokenType; // null if no more tokens
   String currentTokenStringValue;

   public GdqTokenizer(String s) {
      this.s = s;
      this.nextTokenPosition = 0;
      consumeToken();
   }

   GdqTokenType tokenType() {
      return currentTokenType;
   }

   String tokenStringValue() {
      return currentTokenStringValue;
   }

   void consumeToken() {
      currentTokenType = null;
      currentTokenStringValue = null;

      skipWhite();
      if (nextTokenPosition >= s.length()) {
         return;
      }

      if (isDelimiter(s.charAt(nextTokenPosition))) {
         currentTokenStringValue = s.substring(nextTokenPosition, nextTokenPosition+1);
         currentTokenType = GdqTokenType.forStringValue(currentTokenStringValue);
         nextTokenPosition++;
      }
      else {
         // nextTokenPosition points at a word or keyword
         // find first non-word position after that
         int firstNonWord = nextTokenPosition;
         while ( firstNonWord < s.length()
                 && !isDelimiter(s.charAt(firstNonWord))
                 && !Character.isWhitespace(s.charAt(firstNonWord))) {
            firstNonWord++;
         }

         currentTokenStringValue = s.substring(nextTokenPosition, firstNonWord);
         currentTokenType = GdqTokenType.forStringValue(currentTokenStringValue);
         nextTokenPosition = firstNonWord;
      }

   }

   private static boolean isDelimiter(char c) {
      return ",".indexOf(c) != -1;
   }

   private void skipWhite() {
      while ( nextTokenPosition < s.length()
              && Character.isWhitespace(s.charAt(nextTokenPosition))) {
         nextTokenPosition++;
      }
   }

   public boolean hasToken() {
      return currentTokenType != null;
   }

   public boolean hasToken(GdqTokenType type) {
      return currentTokenType == type;
   }

}