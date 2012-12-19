public abstract class Lexer
{
	public static final Integer DEFAULT_TOKEN_CHANNEL = 0;

	public RecognizerSharedState state;
	public CharStream input;

	public Lexer()
	{
	}

	public Lexer(CharStream input, RecognizerSharedState state)
	{
		this.state = state;
		this.input = input;
	}

	public abstract String getGrammarFileName();
	public abstract void mTokens();

	public void match(String s)
	{
		System.debug(s);
	}
}
