package crescentcitydevelopment.com.bam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

public class InviteDialogFragment extends DialogFragment{

    public interface InviteDialogListener{
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    InviteDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.invite_dialog, null))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        //dialog.dismiss();
                        mListener.onDialogNegativeClick(InviteDialogFragment.this);
                    }
                })
                .setPositiveButton("Send Invite", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        //dialog.dismiss();

                        mListener.onDialogPositiveClick(InviteDialogFragment.this);
                    }
                });
                return builder.create();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            mListener = (InviteDialogListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement NoticeDialogListener");
        }
    }
}
