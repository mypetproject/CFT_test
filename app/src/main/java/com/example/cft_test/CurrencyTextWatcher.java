package com.example.cft_test;

import android.text.Editable;
import android.text.TextWatcher;

import com.example.cft_test.databinding.ActivityMainBinding;

public class CurrencyTextWatcher implements TextWatcher {

    String textBeforeChanged = "";
    int selectorLastPosition = 0;
    boolean ignoreNextIteration = true;

    ActivityMainBinding binding;
    MainActivityViewModel model;

    CurrencyTextWatcher(ActivityMainBinding binding) {
        this.binding = binding;
        this.model = binding.getModel();
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        if (!ignoreNextIteration) {
            textBeforeChanged = s.toString();
            selectorLastPosition = binding.rublesTIET.getSelectionStart();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        if (ignoreNextIteration) {
            ignoreNextIteration = false;
        } else {

            if (s.length() >= 20) {

                ignoreNextIteration = true;
                model.setRublesAmount(textBeforeChanged);
                binding.rublesTIL.setError(binding.getRoot().getContext().getString(R.string.error_max_char));

            } else {

                binding.rublesTIL.setErrorEnabled(false);

                if (s.length() > textBeforeChanged.length()) {
                    characterAdded(s);
                } else {
                    characterRemoved(s);
                }
            }
        }

        if (selectorLastPosition < 0) {
            selectorLastPosition = 0;
        } else if (selectorLastPosition > (model.getRublesAmount().length())) {
            selectorLastPosition = model.getRublesAmount().length();
        }

        binding.rublesTIET.setSelection(selectorLastPosition);

        model.setValuteAmount();
    }

    private void characterAdded(Editable s) {
        if (s.charAt(selectorLastPosition) == '.' || s.charAt(selectorLastPosition) == ',') {

            addedDotOrComma(s);

        } else if (selectorLastPosition > textBeforeChanged.length() - 3) {

            addedCharactersInFractionalPart(s);

        } else if (selectorLastPosition == 0 && s.charAt(0) == '0') {

            addedZeroFirst();

        } else {

            formatAfterAddCharacter(s);
        }
    }

    private void characterRemoved(Editable s) {

        if (selectorLastPosition == textBeforeChanged.length() - 2
                || textBeforeChanged.charAt(selectorLastPosition - 1) == ' '
                || textBeforeChanged.charAt(selectorLastPosition - 1) == ',') {

            removedDotCommaOrSpace();

        } else if (selectorLastPosition > textBeforeChanged.length() - 3) {

            removedInFractionalPart(s);

        } else {

            formatAfterRemoveCharacter(s);
        }
    }

    private void addedDotOrComma(Editable s) {
        ignoreNextIteration = true;

        if (selectorLastPosition == s.length() - 4) {
            selectorLastPosition++;
        }

        binding.rublesTIET.setText(textBeforeChanged);
    }

    private void addedCharactersInFractionalPart(Editable s) {
        if (selectorLastPosition == textBeforeChanged.length() - 2) {

            model.setRublesAmount(s.toString().substring(0, selectorLastPosition + 1) + s.toString().substring(selectorLastPosition + 2));
            selectorLastPosition++;

        } else if (selectorLastPosition == textBeforeChanged.length() - 1) {

            selectorLastPosition++;
            model.setRublesAmount(s.toString().substring(0, s.toString().length() - 1));

        } else {

            model.setRublesAmount(textBeforeChanged);

        }
        ignoreNextIteration = true;
    }

    private void addedZeroFirst() {
        model.setRublesAmount(textBeforeChanged);
        ignoreNextIteration = true;
    }

    private void formatAfterAddCharacter(Editable s) {

            model.setRublesAmount(s.toString());

            if (selectorLastPosition > 2
                    || model.getRublesAmount().charAt(1) == ' '
                    || model.getRublesAmount().charAt(1) == ','
                    || s.charAt(0) == '0') {
                ignoreNextIteration = true;
            }
            selectorLastPosition += model.getRublesAmount().length() - textBeforeChanged.length();
    }

    private void removedDotCommaOrSpace() {
        selectorLastPosition--;
        model.setRublesAmount(textBeforeChanged);
        ignoreNextIteration = true;
    }

    private void removedInFractionalPart(Editable s) {
        if (selectorLastPosition == textBeforeChanged.length() - 1) {
            model.setRublesAmount(s.toString().substring(0, selectorLastPosition - 1) + "0" + s.toString().substring(selectorLastPosition - 1));
        } else {
            model.setRublesAmount(s.toString() + '0');
        }
        selectorLastPosition--;
        ignoreNextIteration = true;
    }

    private void formatAfterRemoveCharacter(Editable s) {

            model.setRublesAmount(s.toString());

            if (selectorLastPosition > 3
                    || textBeforeChanged.charAt(selectorLastPosition) == ' '
                    || textBeforeChanged.charAt(selectorLastPosition) == ','
                    || s.length() < 4
                    || (textBeforeChanged.charAt(1) == ' ' && selectorLastPosition == 3)) {

                ignoreNextIteration = true;
            }
            selectorLastPosition += model.getRublesAmount().length() - textBeforeChanged.length();
    }
}

