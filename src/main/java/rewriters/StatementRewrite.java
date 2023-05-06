package rewriters;

import ast.AST;
import ast.AltCase;
import ast.Block;
import ast.DoStat;
import ast.ForStat;
import ast.IfStat;
import ast.Sequence;
import ast.Statement;
import ast.WhileStat;
import utilities.Log;
import utilities.Visitor;

/**
 * This rewrite wraps a single statement into a block if the statement
 * isn't already a block.
 * 
 * @author ben
 */
public class StatementRewrite extends Visitor<AST> {
	
	public StatementRewrite() {
        Log.logHeader("***********************************************");
        Log.logHeader("*      S T A T E M E N T   R E W R I T E      *");
        Log.logHeader("***********************************************");
	}

	// DONE
	@Override
	public AST visitIfStat(IfStat is) {
		Log.log(is, "Visiting an if statement");
		
		if (is.thenpart() != null) {
			// 'then-part' should be a block
			if (!(is.thenpart() instanceof Block))
				is.children[1] = new Block(new Sequence<Statement>(is.thenpart()));
			is.thenpart().visit(this);
		} else
			is.children[1] = new Block(new Sequence<Statement>());
		
		if (is.elsepart() != null) {
			// 'else-part' should be a block
			if (!(is.elsepart() instanceof Block))
				is.children[2] = new Block(new Sequence<Statement>(is.elsepart()));
			is.elsepart().visit(this);
		} else
		    ; // This may be null
		return null;
	}

	// DONE
	@Override
	public AST visitWhileStat(WhileStat ws) {
		Log.log(ws, "Visiting a while statement");
		
		if (ws.stat() != null) {
			// 'stat' should be a block
			if (!(ws.stat() instanceof Block))
				ws.children[1] = new Block(new Sequence<Statement>(ws.stat()));
			ws.stat().visit(this);
		} else
			ws.children[1] = new Block(new Sequence<Statement>());
		return null;
	}

	// DONE
	@Override
	public AST visitForStat(ForStat fs) {
		Log.log(fs, "Visiting a for statement");
		
		if (fs.stats() != null) {
			// 'stat' should be a block
			if (!(fs.stats() instanceof Block))
				fs.children[4] = new Block(new Sequence<Statement>(fs.stats()));
			fs.stats().visit(this);
		} else
			fs.children[4] = new Block(new Sequence<Statement>());
		return null;
	}

	// DONE
	@Override
	public AST visitDoStat(DoStat ds) {
		Log.log(ds, "Visiting a do statement");
		
		if (ds.stat() != null) {
			// 'stat' should be a block
			if (!(ds.stat() instanceof Block))
				ds.children[0] = new Block(new Sequence<Statement>(ds.stat()));
			ds.stat().visit(this);
		} else
			ds.children[0] = new Block(new Sequence<Statement>());
		return null;
	}
	
	// DONE
	@Override
	public AST visitAltCase(AltCase ac) {
		Log.log(ac, "Visiting an alt case");
		
		if (ac.stat() != null) {
			// 'stat' should be a block
			if (!(ac.stat() instanceof Block))
				ac.children[2] = new Block(new Sequence<Statement>(ac.stat())); 
			ac.stat().visit(this);
		} else
			; // This can never be null!
		return null;
	}
	
	// AltStat - nothing to do
}
