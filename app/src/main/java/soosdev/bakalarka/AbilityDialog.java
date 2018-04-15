package soosdev.bakalarka;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

import soosdev.bakalarka.Entities.Temple;


/**
 * Created by patrik on 16.3.2018.
 */

public class AbilityDialog extends DialogFragment {

    public interface AbilityOptions {
        public void increaseHealth(DialogFragment dialog);
        public void increasePillumCount(DialogFragment dialog);
        public void restoreHealth(DialogFragment dialog);
        public void reduceCooldown(DialogFragment dialog);
        public void increaseThrowRange(DialogFragment dialog);
    }

    AbilityOptions options;
    ArrayList<String> strings;
    ArrayList<Boolean> unlocks;
    Temple temple;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String[] abilityNames  = this.strings.toArray(new  String[strings.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose upgrade")
                .setItems(abilityNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        evaluateAction(which);
                    }
                });
        return builder.create();
    }

    private void evaluateAction(int which) {
        short nrOfZeroes = 0;
        for (int i = 0; i <= which + nrOfZeroes; i++) {
            if (unlocks.get(i) == false) nrOfZeroes++;
        }
        doAction(which + nrOfZeroes);
    }

    private void doAction(int number) {
        switch (number) {
            case 0:
                options.restoreHealth(AbilityDialog.this);
                temple.changeState();
                break;
            case 1:
                options.increaseHealth(AbilityDialog.this);
                temple.changeState();
                break;
            case 2:
                options.increasePillumCount(AbilityDialog.this);
                temple.changeState();
                break;
            case 3:
                options.reduceCooldown(AbilityDialog.this);
                temple.changeState();
                break;
            case 4:
                options.increaseThrowRange(AbilityDialog.this);
                temple.changeState();
                break;
            default:break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            options = (AbilityOptions) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AbilityOptions");
        }
    }

    public void setAtributes(ArrayList<String> strings, ArrayList<Boolean> unlocks, Temple temple) {
        this.unlocks = unlocks;
        this.strings = strings;
        this.temple = temple;
    }
}
