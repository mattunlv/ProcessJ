package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.alt.AltCase;
import org.processj.ast.Block;
import org.processj.ast.DoStat;
import org.processj.ast.ForStat;
import org.processj.ast.IfStat;
import org.processj.ast.Sequence;
import org.processj.ast.Statement;
import org.processj.ast.WhileStat;
import org.processj.utilities.Log;
import org.processj.utilities.Visitor;

/**
 * <p>Rewrite {@link Visitor} that wraps a single {@link Statement} into a {@link Block} if not done so already.</p>
 * @see Statement
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class StatementRewrite extends Phase {

	/// -----------
	/// Constructor

	/**
	 * <p>Initializes the {@link StatementRewrite} to its' default state with the specified {@link Phase.Listener}.</p>
	 * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
	 *                 or {@link Phase.Error} messages.
	 * @since 0.1.0
	 */
	public StatementRewrite(final Phase.Listener listener) {
		super(listener);
	}

	/// ------------------------------
	/// org.processj.utilities.Visitor

	/**
	 * <p>Wraps the specified {@link AltCase}'s {@link Statement} in a {@link Block} if it isn't done so already.</p>
	 * @param altCase The {@link AltCase} to mutate.
	 * @since 0.1.0
	 */
	@Override
	public final Void visitAltCase(final AltCase altCase) {

		Log.log(altCase, "Visiting an alt case");

		// If the alt case defines a statement
		if(altCase.definesStatement()) {

			// Check if it's not a block, so it can be wrapped
			if(!(altCase.getStatement() instanceof Block))
				altCase.setStatement(new Block(new Sequence<>(altCase.getStatement())));

			// Recur down
            try {
                altCase.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }

        }

		return null;

	}

	/**
	 * <p>Wraps the specified {@link DoStat}'s {@link Statement} in a {@link Block} if it isn't done so already.</p>
	 * @param doStatement The {@link DoStat} to mutate.
	 * @since 0.1.0
	 */
	@Override
	public final Void visitDoStat(final DoStat doStatement) {

		Log.log(doStatement, "Visiting a do statement");

		// If the Do Statement defines a Statement
		if(doStatement.definesStatement()) {

			// Check if it's not a block, so it can be wrapped
			if(!(doStatement.getStatement() instanceof Block))
				doStatement.setStatement(new Block(new Sequence<>(doStatement.getStatement())));

			// Recur down
            try {
                doStatement.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }

            // Simply wrap it
		} else doStatement.setStatement(new Block(new Sequence<>()));

		return null;

	}

	/**
	 * <p>Wraps the specified {@link ForStat}'s {@link Statement} in a {@link Block} if it isn't done so already.</p>
	 * @param forStatement The {@link ForStat} to mutate.
	 * @since 0.1.0
	 */
	@Override
	public final Void visitForStat(final ForStat forStatement) {

		Log.log(forStatement, "Visiting a for statement");

		// If the For Statement defines a Statement
		if(forStatement.definesStatement()) {

			// Check if it's not a block, so it can be wrapped
			if(!(forStatement.getStatement() instanceof Block))
				forStatement.setStatement(new Block(new Sequence<>(forStatement.getStatement())));

			// Recur down
            try {
                forStatement.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }

            // Simply Wrap it
		} else forStatement.setStatement(new Block(new Sequence<>()));

		return null;

	}

	/**
	 * <p>Wraps the specified {@link IfStat}'s then or else {@link Statement}s in a {@link Block} if it isn't done
	 * so already.</p>
	 * @param ifStatement The {@link IfStat} to mutate.
	 * @since 0.1.0
	 */
	@Override
	public final Void visitIfStat(final IfStat ifStatement) {

		Log.log(ifStatement, "Visiting an if statement");

		// If the If Statement defines a then part
		if(ifStatement.definesThenPart()) {

			// Check if it's not a Block, so it can be wrapped
			if(!(ifStatement.getThenPart() instanceof Block))
				ifStatement.setThenPart(new Block(new Sequence<>(ifStatement.getThenPart())));

			// Recur down
            try {
                ifStatement.getThenPart().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }

        }

		// If the If Statement defines an else part
		if(ifStatement.definesElsePart()) {

			// Check if it's not a Block, so it can be wrapped
			if(!(ifStatement.getElsePart() instanceof Block))
				ifStatement.setElsePart(new Block(new Sequence<>(ifStatement.getElsePart())));

			// Recur down
            try {
                ifStatement.getElsePart().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }

        }

		return null;

	}

	/**
	 * <p>Wraps the specified {@link WhileStat}'s {@link Statement} in a {@link Block} if it isn't done so already.</p>
	 * @param whileStatement The {@link WhileStat} to mutate.
	 * @since 0.1.0
	 */
	@Override
	public final Void visitWhileStat(final WhileStat whileStatement) {

		Log.log(whileStatement, "Visiting a while statement");

		// If the While Statement defines a statement
		if(whileStatement.definesStatement()) {

			// Check if it's not a block, so it can be wrapped
			if(!(whileStatement.getStatement() instanceof Block))
				whileStatement.setStatement(new Block(new Sequence<>(whileStatement.getStatement())));

			// Recur down
            try {
                whileStatement.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }

            // Simply wrap it
		} else whileStatement.setStatement(new Block(new Sequence<>()));

		return null;

	}

}
