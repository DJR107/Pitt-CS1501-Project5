/**
 * HeftyInteger for CS1501 Project 5
 * @author	David Roberts
 */
package cs1501_p5;

import java.util.Random;

public class HeftyInteger {

	private final byte[] ONE = {(byte) 1};

	private final byte[] ZERO = {(byte) 0};

	private byte[] val;

	/**
	 * Construct the HeftyInteger from a given byte array
	 * @param b the byte array that this HeftyInteger should represent
	 */
	public HeftyInteger(byte[] b) 
	{
		val = b;
	}

	/**
	 * Return this HeftyInteger's val
	 * @return val
	 */
	public byte[] getVal() 
	{
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() 
	{
		return val.length;
	}

	/**
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) 
	{
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most
	 * significant byte will be a negative signed number
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() 
	{
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 * @param other the other HeftyInteger to sum with this
	 */
	public HeftyInteger add(HeftyInteger other) 
	{
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		HeftyInteger res_li = new HeftyInteger(res);

		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public HeftyInteger negate() 
	{
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i  = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		HeftyInteger neg_li = new HeftyInteger(neg);

		// add 1 to complete two's complement negation
		return neg_li.add(new HeftyInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 * @param other HeftyInteger to subtract from this
	 * @return difference of this and other
	 */
	public HeftyInteger subtract(HeftyInteger other) 
	{
		return this.add(other.negate());
	}

	/**
	 * Compute the product of this and other
	 * @param other HeftyInteger to multiply by this
	 * @return product of this and other
	 */
	public HeftyInteger multiply(HeftyInteger other) 
	{
		HeftyInteger newThis = new HeftyInteger(val);

		boolean negate = false;
		if (newThis.isNegative() ^ other.isNegative())
			negate = true;

		if (newThis.isNegative())
			newThis = newThis.negate();
		if (other.isNegative())
			other = other.negate();

		HeftyInteger result = recMultHelp(newThis.getVal(), other.getVal());

		if (negate)
			result = result.negate();

		return result.shrink();
	}

	/**
	 * Recursive function to aid in multiplication
	 * @param x HeftyInteger to multiply (this to start)
	 * @param y HeftyInteger to multiply (other to start)
	 *
	 * @return new HeftyInteger that is the result
	 */ 
	private HeftyInteger recMultHelp(byte[] x, byte[] y)
	{
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (x.length < y.length) {
			a = y;
			b = x;
		}
		else {
			a = x;
			b = y;
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		//System.out.println("a length = "+a.length);
		//System.out.println("b length = "+b.length);

		if (a.length == 1)
		{
			return multiply(a[0], b[0]);
		}
		else
		{
			// make a and b even number of bytes by extending each
			if (a.length % 2 == 1)
			{
				byte[] newA = new byte[a.length+1];
				newA[0] = (byte) 0;
				for (int i=0; i<a.length; i++) 
				{
					newA[i+1] = a[i];
				}
				a = newA;

				byte[] newB = new byte[b.length+1];
				newB[0] = (byte) 0;
				for (int i=0; i<b.length; i++) 
				{
					newB[i+1] = b[i];
				}
				b = newB;
			}

			byte[] xH = new byte[a.length/2];
			byte[] xL = new byte[a.length/2];

			byte[] yH = new byte[b.length/2];
			byte[] yL = new byte[b.length/2];

			for (int i=0; i<a.length; i++)
			{
				if (i < xH.length)
					xH[i] = a[i];
				else
					xL[i-xH.length] = a[i];
			}

			for (int i=0; i<b.length; i++)
			{
				if (i < yH.length)
					yH[i] = b[i];
				else
					yL[i-yH.length] = b[i];
			}

			HeftyInteger m1 = recMultHelp(xH, yH);
			HeftyInteger m2 = recMultHelp(xH, yL);
			HeftyInteger m3 = recMultHelp(xL, yH);
			HeftyInteger m4 = recMultHelp(xL, yL);

			HeftyInteger m1shifted = m1.shiftLeft(a.length);

			HeftyInteger m2andm3 = m2.add(m3);

			HeftyInteger m2andm3shifted = m2andm3.shiftLeft(a.length/2);

			HeftyInteger sum = m1shifted.add(m2andm3shifted);

			sum = sum.add(m4);

			return sum;
		}
	}

	/**
	 * Method that does the actual multiplication of two bytes
	 * @param a byte number 1
	 * @param b byte number 2
	 *
	 * @return new HeftryInteger that is the result
	 */
	private HeftyInteger multiply(byte a, byte b)
	{
		byte[] newBs = new byte[2];

		int carry = ((int) a & 0xFF) * ((int) b & 0xFF);

		if (carry > ((1 << 15) - 1))
		{
			//System.out.println("Overflow");
			newBs = new byte[3];
		}
		else if (carry < (-(1 << 15)))
		{
			//System.out.println("Overflow");
			newBs = new byte[3];
		}
			
		for (int i=newBs.length-1; i>=0; i--)
		{
			newBs[i] = (byte) (carry & 0xFF);

			if (i > 0)
				carry = carry >>> 8;
		}

		HeftyInteger newHI = new HeftyInteger(newBs);

		return newHI;
	}

	/**
	 * Shifts a HeftyInteger left n bytes, puts 0's in space behind shift
	 * @param n number of bytes to shift left
	 *
	 * @return new HeftyInteger shifted left n bytes
	 */
	private HeftyInteger shiftLeft(int n)
	{
		byte[] newBs = new byte[val.length+n];

		for (int i=val.length-1; i>=0; i--)
		{
			newBs[i] = val[i];
		}

		return new HeftyInteger(newBs);
	}

	/**
	 * @return new HeftyInteger with all unnecessary leading bytes removed
	 */
	private HeftyInteger shrink()
	{
		int index=0;
		for (int i=0; i<val.length-1; i++)
		{
			index = i;
			if (val[i] > 0 || val[i] < -1)
				break;
			if (val[i] == 0 && val[i+1] < 0)
				break;
			if (val[i] == -1 && val[i+1] > 0)
				break;
		}

		byte[] newVal = new byte[val.length-index];
		for (int j=0; j<newVal.length; j++)
		{
			newVal[j] = val[index];
			index++;
		}

		return new HeftyInteger(newVal);
	}

	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another HeftyInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */
	public HeftyInteger[] XGCD(HeftyInteger other) 
	{
		HeftyInteger[] arr = new HeftyInteger[3];

		if (this.compareTo(other) < 0)
		{
			arr = XGCDrecHelp(other, this, arr);
			HeftyInteger temp = arr[1];
			arr[1] = arr[2];
			arr[2] = temp;
		}
		else 
			arr = XGCDrecHelp(this, other, arr);

		for (int i=0; i<arr.length; i++)
			arr[i] = arr[i].shrink();

		return arr;
	}

	/**
	 * Recursive method to aid in XGCD
	 * @param x One HeftyInteger in XGCD
	 * @param y Another HeftyInteger in XGCD
	 * @param arr Array of GCD, x, and y
	 *
	 * @return arr with all necessary info
	 */
	private HeftyInteger[] XGCDrecHelp(HeftyInteger a, HeftyInteger b, HeftyInteger[] arr)
	{
		HeftyInteger modResult;
		HeftyInteger iterations;

		if (b.isZero())
		{
			arr[0] = a;
			arr[1] = new HeftyInteger(ONE);
			arr[2] = new HeftyInteger(ZERO);
			return arr;
		}
		
		ModMethodReturn modReturn = mod(a, b);
		iterations = modReturn.numOfIterations;
		modResult = modReturn.mod;
		arr = XGCDrecHelp(b, modResult, arr);

		HeftyInteger temp = arr[1];
		arr[1] = arr[2];
		arr[2] = temp.add(iterations.multiply(arr[2]).negate());

		//System.out.print("X -> ");
		//arr[1].print();
		//System.out.print("Y -> ");
		//arr[2].print();

		return arr;
	}

	/**
	 * Finds the mod of x and y (x % y)
	 * @param x One HeftyInteger
	 * @param y HeftyInteger that is subtracted from x to get a remainder
	 *
	 * @return new ModMethodReturn class which includes remainder and # of iterations to that rem
	 * (ModMethodReturn class at bottom)
	 */
	private ModMethodReturn mod(HeftyInteger x, HeftyInteger y)
	{
		//System.out.print("X -> ");
	 	//x.print();
	 	//System.out.print("Y -> ");
	 	//y.print();
		int difInLength = Math.abs(x.length()-y.length());
		//System.out.println("Difference in Length in mod: "+difInLength);
	 	if (difInLength > 3)
		{
			System.out.println("This gon take too long");
			return modForBigDif(x, y, y.shiftLeft(difInLength-1));
		}

	 	HeftyInteger result = new HeftyInteger(x.getVal());
	 	HeftyInteger i = new HeftyInteger(ZERO);
	 	while (result.compareTo(y) >= 0)
	 	{
	 		result = result.add(y.negate());
	 		i = i.add(new HeftyInteger(ONE));
	 	}
	 	//System.out.print("iterations -> ");
	 	//i.print();

		//System.out.print("Result -> ");
	 	//result.print();

	 	return new ModMethodReturn(result, i);
	}

	/**
	 * Finds the mod of x and y (x % y) when y would be much smaller than x and would
	 * require and very large amount of time to subtract repeatedly from x.
	 * This functions scales y up to closely match x's length so an efficient mod can be done
	 * @param x One HeftyInteger
	 * @param y HeftyInteger that is subtracted from x to get a remainder
	 * @param expandedY new HeftyInteger that is y shifted left some bytes
	 *
	 * @return new ModMethodReturn class which includes remainder and # of iterations to that rem
	 * (ModMethodReturn class at bottom)
	 */
	private ModMethodReturn modForBigDif(HeftyInteger x, HeftyInteger y, HeftyInteger expandedY)
	{
		//System.out.print("expandedY -> ");
		//expandedY.print();

		HeftyInteger result = new HeftyInteger(x.getVal());
	 	HeftyInteger i = new HeftyInteger(ZERO);
	 	while (result.compareTo(expandedY) >= 0)
	 	{
	 		result = result.add(expandedY.negate());
	 		i = i.add(new HeftyInteger(ONE));

	 		if (result.compareTo(expandedY) < 0 && result.compareTo(y) > 0)
	 		{
	 			expandedY = expandedY.shiftRight(1);
	 			//System.out.print("new expandedY -> ");
				//expandedY.print();
	 			i = i.shiftLeft(1);
	 			//System.out.print("new i -> ");
				//i.print();
	 		}
	 	}

	 	result = result.shrink();
	 	//System.out.println("Returning");
	 	//System.out.print("result -> ");
	 	//result.print();
	 	//System.out.print("i -> ");
	 	//i.print();
	 	//System.out.println();
	 	return new ModMethodReturn(result, i);
	}

	/**
	 * Shifts a HeftyInteger right n bytes
	 * @param n number of bytes to shift right
	 *
	 * @return new HeftyInteger shifted right n bytes
	 */
	private HeftyInteger shiftRight(int n)
	{
		byte[] newBs = new byte[val.length-n];

		for (int i=0; i<newBs.length; i++)
		{
			newBs[i] = val[i];
		}

		return new HeftyInteger(newBs);
	}

	/**
	 * A comparison method of this and other
	 * @param other HeftyInteger of interest
	 *
	 * @return 1 if this > other
	 * @return -1 if other > this
	 * @return 0 if this == other
	 */
	private int compareTo(HeftyInteger other)
	{
	 	//System.out.println("Comparing");
	 	//System.out.print("this -> ");
	 	//print();
	 	//System.out.print("other -> ");
	 	//other.print();

	 	// If only one of HI is negative
	 	if (isNegative() ^ other.isNegative())
	 	{
	 		if (isNegative())
	 			return -1;
	 		else
	 			return 1;
	 	}

		byte[] a, b;
		boolean swapped = false;

	 	// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
			swapped = true;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		if (swapped)
		{
			byte[] temp = a;
			a = b;
			b = temp;
		}

	 	// Same sign and same length
	 	for (int i=0; i<a.length; i++)
	 	{
	 		int a1 = Byte.toUnsignedInt(a[i]);
	 		int b1 = Byte.toUnsignedInt(b[i]);

	 		//System.out.println(a1);
	 		//System.out.println(b1);

	 		if (a1 > b1)
	 			return 1;
	 		if (a1 < b1)
	 			return -1;
	 	}
	 	return 0;
	}

	/**
	 * @return true is this HeftyInteger is zero
	 */
	private boolean isZero()
	{
		for (int i=0; i<val.length; i++)
		{
			if (val[i] != 0)
				return false;
		}
		return true;
	}

	public void print()
	{
	 	System.out.print("HeftyInteger: ");
	 	for (int i=0; i<val.length; i++)
	 	{
	 		System.out.print(val[i]+" ");
	 	}
	 	System.out.println();
	}

	/**
	 * Special return from mod methods that includes mod and number of iterations
	 * that was required to get that remainder
	 * Both values helpful in calculating X and Y in XGCD
	 */
	private class ModMethodReturn
	{
		protected HeftyInteger mod;
		protected HeftyInteger numOfIterations;

		public ModMethodReturn(HeftyInteger m, HeftyInteger i)
		{
			mod = m;
			numOfIterations = i;
		}
	}
}