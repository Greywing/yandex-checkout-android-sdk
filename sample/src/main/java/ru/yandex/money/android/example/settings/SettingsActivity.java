/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yandex.money.android.example.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import ru.yandex.money.android.example.BuildConfig;
import ru.yandex.money.android.example.R;
import ru.yandex.money.android.example.linkedcards.LinkedCardsActivity;
import ru.yandex.money.android.sdk.Checkout;
import ru.yandex.money.android.sdk.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static ru.yandex.money.android.example.settings.Settings.KEY_GOOGLE_PAY_ALLOWED;
import static ru.yandex.money.android.example.settings.Settings.KEY_GOOGLE_PAY_AVAILABLE;
import static ru.yandex.money.android.example.settings.Settings.KEY_NEW_CARD_ALLOWED;
import static ru.yandex.money.android.example.settings.Settings.KEY_PAYMENT_AUTH_PASSED;
import static ru.yandex.money.android.example.settings.Settings.KEY_SBERBANK_ONLINE_ALLOWED;
import static ru.yandex.money.android.example.settings.Settings.KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR;
import static ru.yandex.money.android.example.settings.Settings.KEY_SHOW_YANDEX_CHECKOUT_LOGO;
import static ru.yandex.money.android.example.settings.Settings.KEY_TEST_MODE_ENABLED;
import static ru.yandex.money.android.example.settings.Settings.KEY_YANDEX_MONEY_ALLOWED;
import static ru.yandex.money.android.sdk.Checkout.EXTRA_ERROR_CODE;
import static ru.yandex.money.android.sdk.Checkout.EXTRA_ERROR_DESCRIPTION;
import static ru.yandex.money.android.sdk.Checkout.EXTRA_ERROR_FAILING_URL;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int START_CONFIRMATION_REQUEST_CODE = 1;

    private List<Pair<String, Integer>> keysToIds = Arrays.asList(
            new Pair<>(KEY_YANDEX_MONEY_ALLOWED, R.id.payment_option_yamoney),
            new Pair<>(KEY_SBERBANK_ONLINE_ALLOWED, R.id.payment_option_sberbank_online),
            new Pair<>(KEY_GOOGLE_PAY_ALLOWED, R.id.payment_option_google_pay),
            new Pair<>(KEY_NEW_CARD_ALLOWED, R.id.payment_option_new_card),
            new Pair<>(KEY_SHOW_YANDEX_CHECKOUT_LOGO, R.id.enable_yandex_checkout_logo),
            new Pair<>(KEY_TEST_MODE_ENABLED, R.id.enable_test_mode),
            new Pair<>(KEY_PAYMENT_AUTH_PASSED, R.id.payment_auth_passed),
            new Pair<>(KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR, R.id.complete_with_error),
            new Pair<>(KEY_GOOGLE_PAY_AVAILABLE, R.id.google_pay_available)
    );
    private View linkedCardsButton;
    private CompoundButton paymentAuthPassedSwitch;
    private CompoundButton completeWithErrorButton;
    private CompoundButton googlePayAvailable;
    private View paymentAuthPassedDivider;
    private View linkedCardsDivider;
    private View completeWithErrorDivider;
    private View googlePayAvailableDivider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));

        linkedCardsButton = findViewById(R.id.linked_cards);
        linkedCardsButton.setOnClickListener(this);

        final CompoundButton enableTestModeSwitch = findViewById(R.id.enable_test_mode);
        paymentAuthPassedSwitch = findViewById(R.id.payment_auth_passed);
        completeWithErrorButton = findViewById(R.id.complete_with_error);
        googlePayAvailable = findViewById(R.id.google_pay_available);
        paymentAuthPassedDivider = findViewById(R.id.payment_auth_passed_divider);
        linkedCardsDivider = findViewById(R.id.linked_cards_divider);
        completeWithErrorDivider = findViewById(R.id.complete_with_error_divider);
        googlePayAvailableDivider = findViewById(R.id.google_pay_available_divider);

        enableTestModeSwitch.setOnCheckedChangeListener(
                (v, isChecked) -> setTestGroupVisibility(isChecked ? View.VISIBLE : View.GONE));

        setTestGroupVisibility(enableTestModeSwitch.isChecked() ? View.VISIBLE : View.GONE);

        final Settings settings = new Settings(this);

        this.<CompoundButton>findViewById(R.id.payment_option_yamoney).setChecked(settings.isYandexMoneyAllowed());
        this.<CompoundButton>findViewById(R.id.payment_option_new_card).setChecked(settings.isNewCardAllowed());
        this.<CompoundButton>findViewById(R.id.payment_option_sberbank_online)
                .setChecked(settings.isSberbankOnlineAllowed());
        this.<CompoundButton>findViewById(R.id.payment_option_google_pay)
                .setChecked(settings.isGooglePayAllowed());
        this.<CompoundButton>findViewById(R.id.enable_yandex_checkout_logo)
                .setChecked(settings.showYandexCheckoutLogo());
        enableTestModeSwitch.setChecked(settings.isTestModeEnabled());
        paymentAuthPassedSwitch.setChecked(settings.isPaymentAuthPassed());
        completeWithErrorButton.setChecked(settings.shouldCompletePaymentWithError());
        googlePayAvailable.setChecked(settings.isGooglePayAvailable());

        final String versionInfo = getString(
                R.string.version_template, BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE, BuildConfig.VERSION_CODE);
        this.<TextView>findViewById(R.id.version_info).setText(versionInfo);

        final View open3dsButton = findViewById(R.id.open3ds);
        if (BuildConfig.DEBUG) {
            open3dsButton.setVisibility(View.VISIBLE);
            open3dsButton.setOnClickListener(
                    v -> {
                        try {
                            startActivityForResult(
                                    Checkout.create3dsIntent(
                                            this,
                                            new URL("file:///android_asset/test.html"),
                                            new URL("http://redirect.url.com/")),
                                    START_CONFIRMATION_REQUEST_CODE);
                        } catch (MalformedURLException e) {
                            Log.e("error", "url exception", e);
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            savePreferences();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        savePreferences();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Bundle extras = data == null ? null : data.getExtras();

        if (requestCode == START_CONFIRMATION_REQUEST_CODE && extras != null) {
            final String message;

            if (resultCode == RESULT_OK) {
                message = "Result OK";
            } else {
                message = "errorCode=" + extras.get(EXTRA_ERROR_CODE) + "\n" +
                        "errorDescription=" + extras.get(EXTRA_ERROR_DESCRIPTION) + "\n" +
                        "errorFailingUrl=" + extras.get(EXTRA_ERROR_FAILING_URL);
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.linked_cards) {
            startActivity(new Intent(this, LinkedCardsActivity.class));
        }
    }

    private void setTestGroupVisibility(int visibility) {
        paymentAuthPassedSwitch.setVisibility(visibility);
        linkedCardsButton.setVisibility(visibility);
        completeWithErrorButton.setVisibility(visibility);
        googlePayAvailable.setVisibility(visibility);
        paymentAuthPassedDivider.setVisibility(visibility);
        linkedCardsDivider.setVisibility(visibility);
        completeWithErrorDivider.setVisibility(visibility);
        googlePayAvailableDivider.setVisibility(visibility);
    }

    private void savePreferences() {
        final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();

        for (Pair<String, Integer> keyToId : keysToIds) {
            edit.putBoolean(keyToId.first, this.<CompoundButton>findViewById(keyToId.second).isChecked());
        }

        edit.apply();

        final Settings settings = new Settings(this);
        final Configuration configuration = new Configuration(
                settings.isTestModeEnabled(),
                settings.shouldCompletePaymentWithError(),
                settings.isPaymentAuthPassed(),
                settings.getLinkedCardsCount(),
                settings.isGooglePayAvailable(),
                settings.isTestModeEnabled()
        );

        Checkout.configureTestMode(configuration);
    }
}
