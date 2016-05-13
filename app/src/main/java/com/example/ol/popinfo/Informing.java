package com.example.ol.popinfo;

/**
 * Created by ol on 07.04.16.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

public class Informing {

  /**
   * failure informing dialog
   */
  public static class ServiceFailedDialogFragment extends DialogFragment {

    public static ServiceFailedDialogFragment newInstance(
        int titleId, int messageId, int iconId) {
      ServiceFailedDialogFragment frag = new ServiceFailedDialogFragment();
      Bundle args = new Bundle();
      args.putInt("titleId", titleId);
      args.putInt("messageId", messageId);
      args.putInt("iconId", iconId);
      frag.setArguments(args);
      return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      int titleId = getArguments().getInt("titleId");
      int messageId = getArguments().getInt("messageId");
      int iconId = getArguments().getInt("iconId");

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(titleId)
          .setMessage(messageId)
          .setIcon(iconId)
          .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
          });
      return builder.create();
    }
  }

  /**
   * failure informing dialog with action button
   */
  public static class ServiceFixDialogFragment extends DialogFragment {

    public static ServiceFixDialogFragment newInstance(
        int titleId, int messageId, int iconId, int posBtTitleId, int negBtTitleId) {
      ServiceFixDialogFragment frag = new ServiceFixDialogFragment();
      Bundle args = new Bundle();
      args.putInt("titleId", titleId);
      args.putInt("messageId", messageId);
      args.putInt("iconId", iconId);
      args.putInt("posBtTitleId", posBtTitleId);
      args.putInt("negBtTitleId", negBtTitleId);
      frag.setArguments(args);
      return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      int titleId = getArguments().getInt("titleId");
      int messageId = getArguments().getInt("messageId");
      int iconId = getArguments().getInt("iconId");
      int posBtTitleId = getArguments().getInt("posBtTitleId");
      int negBtTitleId = getArguments().getInt("negBtTitleId");

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(titleId)
          .setMessage(messageId)
          .setIcon(iconId);
      builder.setPositiveButton(posBtTitleId, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
          getActivity().startActivity(myIntent);
        }
      });
      builder.setNegativeButton(negBtTitleId, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          getActivity().finish();
        }
      });
      return builder.create();
    }
  }

}