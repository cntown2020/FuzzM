/* 
 * Copyright (C) 2017, Rockwell Collins
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the 3-clause BSD license.  See the LICENSE file for details.
 * 
 */
package fuzzm.value.hierarchy;

import jkind.lustre.NamedType;
import jkind.lustre.Type;

abstract public class EvaluatableType<T extends InstanceType<T>> extends EvaluatableValueHierarchy {

	public abstract EvaluatableType<BooleanType> equalop(EvaluatableValue right);

	public abstract EvaluatableType<T> join(EvaluatableValue right);
	
	public abstract T getHigh();
	
	public abstract T getLow();
	
	abstract public EvaluatableType<IntegerType>  integerType();
	abstract public EvaluatableType<RationalType> rationalType();
	abstract public EvaluatableType<BooleanType> booleanType();
	
	@Override
	public final EvaluatableValue cast(Type type) {
		if (type == NamedType.BOOL) return booleanType();
		if (type == NamedType.INT)  return integerType();
		if (type == NamedType.REAL) return rationalType();
		throw new IllegalArgumentException();
	}
	
}
