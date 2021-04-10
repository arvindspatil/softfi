package com.arvind.controller;

public class Result {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Error arguments");
			return;
		}
		int n = Integer.getInteger(args[0]);
		fixxBuzz(n);
	}

	public static void main1(String[] args) {
		if (args.length != 1) {
			System.out.println("Error arguments");
			return;
		}
		int n = Integer.getInteger(args[0]);
		fixxBuzz(n);
	}

	public static void fixxBuzz(int n) {
		for (int i = 1; i <= n; i++) {
			boolean threeFlg = (i % 3 == 0);
			boolean fiveFlg = (i % 5 == 0);
			if (threeFlg && fiveFlg) {
				System.out.println("Both");
			} else if (threeFlg && !fiveFlg) {
				System.out.println("Only Three");
			} else if (!threeFlg && fiveFlg) {
				System.out.println("Only Five");
			} else {
				System.out.println("None");
			}
		}
	}
}
