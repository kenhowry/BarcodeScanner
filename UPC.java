package HW1;

import java.util.Arrays;

// This is the starting version of the UPC-A scanner
//   that needs to be filled in for the homework

public class UPC {
	//--------------------------------------------
	// Scan in the bit pattern from the image
	// Takes the filename of the image
	// Returns an int array of the 95 scanned bits
	//--------------------------------------------
	public static int[] scanImage(String filename) {
		//variable declaration
		int[] scan = new int[95];
		final int whiteSpace = 255;
		int index = 0;
		
		DUImage barCodeImage = new DUImage("barcode1Mutation.png");
		
		//for-loop to scan the image into array of binary numbers
		for (int i = 5; i < 200; i+=2) {
			int color = barCodeImage.getGreen(i, 50);
	
			if (color != whiteSpace) {
				scan[index] = 1;
			}
			index ++;
		}
		//returning the array
		return scan;
	}
	
	//--------------------------------------------
	// Finds the matching digit for the given pattern
	// This is a helper method for decodeScan
	// Takes the full 95 scanned pattern as well as
	//   a starting location in that pattern where we
	//   want to look
	// Also takes in a boolean to indicate if this is a
	//   left or right pattern
	// Returns an int indicating which digit matches
	//   Any pattern that doesn't match anything will be -1
	//--------------------------------------------
	public static int matchPattern(int[] scanPattern, int startIndex, boolean left) {
		
		int[][] digitPat = {{0,0,0,1,1,0,1},
				            {0,0,1,1,0,0,1},	
				            {0,0,1,0,0,1,1},
				            {0,1,1,1,1,0,1},
				            {0,1,0,0,0,1,1},
				            {0,1,1,0,0,0,1},
				            {0,1,0,1,1,1,1},
				            {0,1,1,1,0,1,1},
				            {0,1,1,0,1,1,1},
				            {0,0,0,1,0,1,1}};
		
		//taking an copy of the array section
		int[] arraySection = Arrays.copyOfRange(scanPattern, startIndex, startIndex + 7);

		int rowNumber = -1;
		
		//copying the L pattern into R
		int [][] digitPatR = new int[digitPat.length][];
		
		for(int i = 0; i < digitPat.length; i++) {
			digitPatR[i] = digitPat[i].clone();
		}
		
		for (int i = 0; i < 10; i++) {
			
			for (int j = 0; j < 7; j++) {
				
				if (digitPatR[i][j] == 0) {
					digitPatR[i][j] = 1;
				}
				else {
					digitPatR[i][j] = 0;
				}
			}
		}
		
		//for loop comparing the L pattern
		if (left == true){
		
			for (int i = 0; i < 10; i++) {
				if (Arrays.equals(arraySection, digitPat[i])) {
					rowNumber = i;
					break;
				}
			}
		}
		
		//for loop comparing the R pattern
		else {
			for (int i = 0; i < 10; i++) {
				if (Arrays.equals(arraySection, digitPatR[i])) {
					rowNumber = i;
					break;
				}
			}
		}
		return rowNumber;
	}

	//--------------------------------------------
	// Performs a full scan decode that turns all 95 bits
	//   into 12 digits
	// Takes the full 95 bit scanned pattern
	// Returns an int array of 12 digits
	//   If any digit scanned incorrectly it is returned as a -1
	// If the start, middle, or end patterns are incorrect
	//   it provides an error and exits
	//--------------------------------------------
	public static int[] decodeScan(int[] scanPattern) {
		
		//variable declaration
		int[] startPattern = {1,0,1};
		int[] middlePattern = {0,1,0,1,0};
		int[] endPattern = {1,0,1};
		
		int[] scanCode = new int [15];
		int scanCodeIndex = 0;
		
		int[] startSection = Arrays.copyOfRange(scanPattern, 0, startPattern.length);
		int[] middleSection = Arrays.copyOfRange(scanPattern, startPattern.length + 42, startPattern.length + 42 + middlePattern.length);
		int[] endSection = Arrays.copyOfRange(scanPattern, scanPattern.length - endPattern.length, scanPattern.length);

		//checking if the start, middle, and end patterns match
		if ((Arrays.equals(startPattern, startSection)) && (Arrays.equals(middlePattern, middleSection)) &&
				   (Arrays.equals(endPattern, endSection))){
			//decoding the L pattern
			for (int i = 3; i < startPattern.length + 42; i += 7) {
				scanCode[scanCodeIndex] = matchPattern(scanPattern, i, true);
				scanCodeIndex ++;
			}
			//decoding the R pattern
			for (int i = startPattern.length + 42 + middlePattern.length; i < 95; i += 7) {
				scanCode[scanCodeIndex] = matchPattern(scanPattern, i, false);
				scanCodeIndex ++;
			}
		}
		//if the start, middle, and end do not match, exit
		else {
			System.out.println("Invalid Bar Code.");
			System.exit(1);
		}
		return scanCode;
	}
	
	//--------------------------------------------
	// Do the checksum of the digits here
	// All digits are assumed to be in range 0..9
	// Returns true if check digit is correct and false otherwise
	//--------------------------------------------
	public static boolean verifyCode(int[] digits) {
		
		//In the UPC-A system, the check digit is calculated as follows:
		//	1.Add the digits in the even-numbered positions (zeroth, second, fourth, sixth, etc.) together and multiply by three.
		//	2.Add the digits in the odd-numbered positions (first, third, fifth, etc.) to the result.
		//	3.Find the result modulo 10 (i.e. the remainder when divided by 10.. 10 goes into 58 5 times with 8 leftover).
		//	4.If the result is not zero, subtract the result from ten.

		// Note that what the UPC standard calls 'odd' are our evens since we are zero based and they are one based
		
		//variable declaration
		int evenResult = 0;
		int oddResult = 0;
		int result = 0;
		int checkDigit = 0;
		
		//check digit calculation
		for (int i = 0; i < 12; i++) {
			if (i % 2 == 0){
				evenResult += digits[i];
			}
			else {
				oddResult += digits[i];
			}
		}
		
		result = evenResult * 3;
		result = result + oddResult;
		result = result % 10;
		
		//checking if the check digit is between 0-9
		if (result != 0){
			checkDigit = 10 - result;
		}
		else {
			checkDigit = result;
		}
		
		if (checkDigit < 10 && checkDigit >= 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//--------------------------------------------
	// The main method scans the image, decodes it,
	//   and then validates it
	//--------------------------------------------	
	public static void main(String[] args) {
	        // file name to process.
	        // Note: change this to other files for testing
	        String barcodeFileName = "barcode1.png";

	        // optionally get file name from command-line args
	        if(args.length == 1){
		    barcodeFileName = args[0];
		}
		
		// scanPattern is an array of 95 ints (0..1)
		int[] scanPattern = scanImage(barcodeFileName);

		// Display the bit pattern scanned from the image
		System.out.println("Original scan");
		for (int i=0; i<scanPattern.length; i++) {
			System.out.print(scanPattern[i]);
		}
		System.out.println(""); // the \n
				
		
		// digits is an array of 12 ints (0..9)
		int[] digits = decodeScan(scanPattern);
		
		// YOUR CODE HERE TO HANDLE UPSIDE-DOWN SCANS
		
		if (digits[0] == -1) {
			for(int i = 0; i < scanPattern.length / 2; i++)
			{
			    int temp = scanPattern[i];
			    scanPattern[i] = scanPattern[scanPattern.length - i - 1];
			    scanPattern[scanPattern.length - i - 1] = temp;
			}
		
		digits = decodeScan(scanPattern);
		
		}
		
		// Display the digits and check for scan errors
		boolean scanError = false;
		System.out.println("Digits");
		for (int i=0; i<12; i++) {
			System.out.print(digits[i] + " ");
			if (digits[i] == -1) {
				scanError = true;
			}
		}
		System.out.println("");
				
		if (scanError) {
			System.out.println("Scan error");
			
		} else { // Scanned in correctly - look at checksum
		
			if (verifyCode(digits)) {
				System.out.println("Passed Checksum");
			} else {
				System.out.println("Failed Checksum");
			}
		}
	}
}

