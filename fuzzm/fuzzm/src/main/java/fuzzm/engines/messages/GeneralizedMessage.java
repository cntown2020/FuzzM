/* 
 * Copyright (C) 2017, Rockwell Collins
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the 3-clause BSD license.  See the LICENSE file for details.
 * 
 */
package fuzzm.engines.messages;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import fuzzm.engines.EngineName;
import fuzzm.lustre.BooleanCtx;
import fuzzm.lustre.ExprCtx;
import fuzzm.lustre.ExprSignal;
import fuzzm.lustre.generalize.PolyGeneralizationResult;
import fuzzm.poly.VariableID;
import fuzzm.util.FuzzmName;
import fuzzm.util.IntervalVector;
import fuzzm.util.RatSignal;
import jkind.lustre.BinaryOp;
import jkind.lustre.RealExpr;
import jkind.util.BigFraction;

/**
 * The Generalized Message Iterator allows iteration over
 * the entire state space of the generalized counterexample.
 *
 */
class GeneralizedMessageIterator implements Iterator<Long> {

	long count;
	
	public static final long MAX_MODEL_SIZE = 1000L;
    
    
	public GeneralizedMessageIterator() {
		count = MAX_MODEL_SIZE;
	}
	
	@Override
	public boolean hasNext() {
		return count > 0;
	}

	@Override
	public Long next() {
		return new Long(count--);
	}
	
}

/**
 * The Generalized message contains a generalized solution to the
 * constraint.
 * 
 * Generated by the Generalization Engine.
 * Consumed by the Evaluator and Test Heuristic Engines.
 */
public class GeneralizedMessage extends FeatureMessage implements Iterable<Long> {

	public final double        time;
    public final RatSignal     counterExample;
	//public FuzzMModel    cex;
	//public List<VarDecl> inputNames;
	public final RatSignal     generalizationTarget;
	//public final IntervalSignal generalizedCEX;
	public final PolyGeneralizationResult polyCEX;
	
	private GeneralizedMessage(EngineName source, FeatureID id, String name, double time, RatSignal generalizationTarget, RatSignal counterExample, PolyGeneralizationResult polyCEX, long sequence) {
		super(source,QueueName.GeneralizedMessage,id,name,sequence);
		assert(polyCEX.result.cex && !polyCEX.result.isNegated());
		this.counterExample = counterExample;
		//this.generalizedCEX    = generalizedCEX;
		//this.inputNames = inputNames;
		assert(generalizationTarget != null);
		//assert(target.size() > 0);
		this.generalizationTarget = generalizationTarget;
		this.polyCEX = polyCEX;
		this.time = time;
	}
	
	public GeneralizedMessage(EngineName source, CounterExampleMessage m, PolyGeneralizationResult polyCEX) {
		this(source,m.id,m.name,m.time,m.generalizationTarget,m.counterExample,polyCEX,m.sequence);
	}
	
	public RatSignal nextBiasedVector(boolean biased, BigFraction min, BigFraction max, IntervalVector span, Map<VariableID,BigFraction> ctx) {
		return polyCEX.result.randomVector(biased,min,max,span,ctx);
	}
	
	@Override
	public void handleAccept(MessageHandler handler) {
		handler.handleMessage(this);
	}

	@Override
	public Iterator<Long> iterator() {
		return new GeneralizedMessageIterator();
	}
	
	@Override
	public String toString() {
		return "Message: [Generalized] " + sequence + ":" + id; //  + " :\n" + generalizedCEX.toString();
	}
	
//	private static Expr inBounds(int time, TypedName name, BigFraction low, BigFraction hi) {
//		Expr timeBound = new BinaryExpr(new IdExpr(FuzzMName.time),BinaryOp.EQUAL,new IntExpr(time));
//		Expr hiBound   = new BinaryExpr(Rat.cast(name.name, name.type),BinaryOp.LESSEQUAL,Rat.toExpr(hi));
//		Expr lowBound  = new BinaryExpr(Rat.toExpr(low),BinaryOp.LESSEQUAL,Rat.cast(name.name,name.type));
//		Expr lohiBound = new BinaryExpr(hiBound,BinaryOp.AND,lowBound);
//		//Expr outside   = new UnaryExpr(UnaryOp.NOT,lohiBound);
//		Expr outBound  = new BinaryExpr(timeBound,BinaryOp.IMPLIES,lohiBound);
//		return outBound;
//	}
//	
//	private static Expr lowerBound(int time, TypedName name, BigFraction low) {
//		Expr timeBound = new BinaryExpr(new IdExpr(FuzzMName.time),BinaryOp.EQUAL,new IntExpr(time));
//		Expr lowBound  = new BinaryExpr(Rat.toExpr(low),BinaryOp.LESSEQUAL,Rat.cast(name.name,name.type));
//		//Expr outside   = new UnaryExpr(UnaryOp.NOT,lohiBound);
//		Expr outBound  = new BinaryExpr(timeBound,BinaryOp.IMPLIES,lowBound);
//		return outBound;
//	}
//	
//	private static Expr upperBound(int time, TypedName name, BigFraction hi) {
//		Expr timeBound = new BinaryExpr(new IdExpr(FuzzMName.time),BinaryOp.EQUAL,new IntExpr(time));
//		Expr hiBound   = new BinaryExpr(Rat.cast(name.name, name.type),BinaryOp.LESSEQUAL,Rat.toExpr(hi));
//		//Expr outside   = new UnaryExpr(UnaryOp.NOT,lohiBound);
//		Expr outBound  = new BinaryExpr(timeBound,BinaryOp.IMPLIES,hiBound);
//		return outBound;
//	}
	
	// bounding_box = not body
	// delay = pre bounding_box
	// outside = not body
	// rhs = (not body) OR (pre bounding_box)
	// res = not body -> (not body) OR (pre bounding_box)
//	private static Expr notAlways(IdExpr name,Expr body) {
//		Expr outside = new UnaryExpr(UnaryOp.NOT,body);
//		Expr delay = new UnaryExpr(UnaryOp.PRE,name);
//		Expr rhs = new BinaryExpr(outside,BinaryOp.OR,delay);
//		Expr res = new BinaryExpr(outside,BinaryOp.ARROW,rhs);
//		return res;
//	}
	
//	public BooleanCtx bounds(IntervalVector span) {
//		List<Expr> boundByTime = new ArrayList<>();
//		for (int time=0;time<counterExample.size();time++) {
//			IntervalVector iv = generalizedCEX.get(time);
//			for (TypedName name: iv.keySet()) {
//				FuzzMInterval z = iv.get(name);
//				BigFraction low = z.min;
//				BigFraction hi  = z.max;
//				BigFraction spanLow = span.get(name).min;
//				BigFraction spanHigh = span.get(name).max;
//				boolean redundantLow  = low.compareTo(spanLow) <= 0;
//				boolean redundantHigh = hi.compareTo(spanHigh) >= 0;
//				if (! (redundantLow || redundantHigh)) {
//					boundByTime.add(inBounds(time,name,low,hi));
//				} else if (! redundantHigh) {
//					boundByTime.add(upperBound(time,name,hi));
//				} else if (! redundantLow) {
//					boundByTime.add(lowerBound(time,name,low));
//				}
//			}
//		}
//		BooleanCtx ctx = new BooleanCtx(boundByTime);
//		ctx.bind(FuzzMName.boundingBox + "__");
//		Expr boundByTimeExpr = ctx.getExpr();
//		IdExpr name = new IdExpr(FuzzMName.boundingBox);
//		name = ctx.define(FuzzMName.boundingBox, NamedType.BOOL,notAlways(name,boundByTimeExpr));
//		ctx.setExpr(name);
//		return ctx;
//	}
	
	public BooleanCtx dot(IntervalVector span) {
		int k = generalizationTarget.size();
		ExprSignal inputs = span.getExprSignal(k);
		inputs = inputs.sub(counterExample);
		RatSignal vector = generalizationTarget.sub(counterExample);
		ExprCtx dot = inputs.dot(vector, FuzzmName.pivotDot);
		dot.op(BinaryOp.GREATEREQUAL,new RealExpr(BigDecimal.ZERO));
		dot.bind(FuzzmName.pivotDot);
		return new BooleanCtx(dot);
	}
	
	@Override
	public int bytes() {
		return 1 + counterExample.bytes() + polyCEX.result.bytes();
	}
	
}
