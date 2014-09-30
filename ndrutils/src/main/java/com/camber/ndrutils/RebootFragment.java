package com.camber.ndrutils;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.camber.ndrutils.util.AlertUtil;
import com.camber.ndrutils.util.PropUtil;

public class RebootFragment extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton rcvPhilz, rcvTwrp, rcvCwn;
    private LinearLayout recoverySelection, recoveryMissing;

    private static final String CMD_REBOOT = "svc power reboot";
    private static final String CMD_REBOOT_RECOVERY = CMD_REBOOT + " recovery";
    private static final String CMD_REBOOT_BOOTLOADER = CMD_REBOOT + " bootloader";
    private static final String CMD_REBOOT_FLASH = CMD_REBOOT + " oem-53";
    private static final String CMD_PWR_OFF = "svc power shutdown";
    /*** Original Quick Reboot code ***/
    /*private static final String CMD_REBOOT_QUICK = "setprop ctl.restart surfaceflinger && setprop ctl.restart zygote";*/
    /*** New Quick Reboot code ***/
    private static final String CMD_REBOOT_QUICK = "stop zygote; sleep 1; start zygote;";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reboot, container, false);
        Button pwr_off = (Button) rootView.findViewById(R.id.click_pwr_off);
        Button pwr_recovery = (Button) rootView.findViewById(R.id.click_pwr_recovery);
        Button pwr_bootloader = (Button) rootView.findViewById(R.id.click_pwr_bootloader);
        Button pwr_flash = (Button) rootView.findViewById(R.id.click_pwr_flash);
        Button pwr_reboot = (Button) rootView.findViewById(R.id.click_pwr_reboot);
        Button pwr_reboot_quick = (Button) rootView.findViewById(R.id.click_pwr_reboot_quick);

        pwr_off.setOnClickListener(btnClickReboot);
        pwr_recovery.setOnClickListener(btnClickReboot);
        pwr_bootloader.setOnClickListener(btnClickReboot);
        pwr_flash.setOnClickListener(btnClickReboot);
        pwr_reboot.setOnClickListener(btnClickReboot);
        pwr_reboot_quick.setOnClickListener(btnClickReboot);

        /*** Disabled KitKat check for now, trying new hot reboot code ***/
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pwr_reboot_quick.setVisibility(View.GONE);
        }*/

        rcvPhilz = (RadioButton) rootView.findViewById(R.id.radioButtonPhilz);
        rcvTwrp = (RadioButton) rootView.findViewById(R.id.radioButtonTwrp);
        rcvCwn = (RadioButton) rootView.findViewById(R.id.radioButtonCwm);

        rcvPhilz.setOnClickListener(onRadioButtonClicked);
        rcvTwrp.setOnClickListener(onRadioButtonClicked);
        rcvCwn.setOnClickListener(onRadioButtonClicked);

        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);

        recoverySelection = (LinearLayout) rootView.findViewById(R.id.recoverySelection);
        recoveryMissing = (LinearLayout) rootView.findViewById(R.id.recoveryMissing);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    View.OnClickListener onRadioButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton) view).isChecked();
            switch (view.getId()) {
                case R.id.radioButtonPhilz:
                    if (checked)
                        PropUtil.updatePropFile(0);
                    break;
                case R.id.radioButtonTwrp:
                    if (checked)
                        PropUtil.updatePropFile(1);
                    break;
                case R.id.radioButtonCwm:
                    if (checked)
                        PropUtil.updatePropFile(2);
                    break;
            }
        }
    };

    View.OnClickListener btnClickReboot = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.click_pwr_off:
                    AlertUtil.showRebootAlert(NdrApp.getInstance().getString(R.string.pwr_message_shutdown), CMD_PWR_OFF);
                    break;
                case R.id.click_pwr_recovery:
                    AlertUtil.showRebootAlert(NdrApp.getInstance().getString(R.string.pwr_message_recovery), CMD_REBOOT_RECOVERY);
                    break;
                case R.id.click_pwr_bootloader:
                    AlertUtil.showRebootAlert(NdrApp.getInstance().getString(R.string.pwr_message_bootloader), CMD_REBOOT_BOOTLOADER);
                    break;
                case R.id.click_pwr_flash:
                    AlertUtil.showRebootAlert(NdrApp.getInstance().getString(R.string.pwr_message_flash), CMD_REBOOT_FLASH);
                    break;
                case R.id.click_pwr_reboot:
                    AlertUtil.showRebootAlert(NdrApp.getInstance().getString(R.string.pwr_message_reboot), CMD_REBOOT);
                    break;
                case R.id.click_pwr_reboot_quick:
                    AlertUtil.showRebootAlert(NdrApp.getInstance().getString(R.string.pwr_message_reboot_fast), CMD_REBOOT_QUICK);
                    break;
            }
        }
    };

    private void updateUI() {
        radioGroup.setEnabled(true);
        switch (PropUtil.readPropFile()) {
            case 2:
                radioGroup.check(rcvCwn.getId());
                break;
            case 1:
                radioGroup.check(rcvTwrp.getId());
                break;
            case 0:
                radioGroup.check(rcvPhilz.getId());
                break;
            default:
                recoveryMissing.setVisibility(View.VISIBLE);
                recoverySelection.setVisibility(View.GONE);
                break;
        }
    }
}
