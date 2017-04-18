package lox24sms.lox24sms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberValidator {

	private static final int PHONE_NUMBER_MIN_LENGTH = 5;
	private static final int PHONE_NUMBER_MAX_LENGTH = 15;

	private PhoneNumberValidator() {
	}

	public static boolean validatePhoneNumber(String phoneNumber) {
		return validatePhoneNumber(phoneNumber, false);
	}

	public static boolean validatePhoneNumber(String phoneNumber, boolean skipLengthCheck) {
		if (isEmpty(phoneNumber)) {
			return false;
		}
		if (!hasPlusSign(phoneNumber)) {
			return false;
		}
		if (hasInvalidChars(phoneNumber.substring(1, phoneNumber.length()))) {
			return false;
		}

		if (!skipLengthCheck) {
			String trimPhoneNumber = phoneNumber.substring(1);
			if (!validLength(trimPhoneNumber)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isEmpty(String origPhoneNum) {
		if (origPhoneNum == null || origPhoneNum.trim().equals("")) {
			return true;
		}
		return false;
	}

	private static boolean hasPlusSign(String origPhoneNum) {
		if (origPhoneNum.charAt(0) != '+') {
			return false;
		}
		return true;
	}

	public static boolean hasInvalidChars(String phoneNumber) {
		String invalidCharFound = findNonDigitChars(phoneNumber);
		if (invalidCharFound != null) {
			return true;
		}
		return false;
	}

	private static boolean validLength(String phoneNumber) {
		if (phoneNumber == null) {
			return false;
		}
		return (phoneNumber.length() <= PHONE_NUMBER_MAX_LENGTH) && (phoneNumber.length() >= PHONE_NUMBER_MIN_LENGTH);
	}

	public static String findNonDigitChars(String str) {
		if (str == null) {
			return null;
		}

		final Pattern p = Pattern.compile("[^0-9]");
		final Matcher m = p.matcher(str);

		boolean found = m.find();
		if (!found) {
			return null;
		} else {
			return m.group();
		}
	}
}
