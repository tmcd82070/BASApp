package com.west.bas.ui;

public abstract class PrivacyPolicyCallback {
	public abstract void onTaskComplete(
			boolean hasAcceptedPrivacyPolicy,
			boolean hasProvidedConsentToLocation);
}
