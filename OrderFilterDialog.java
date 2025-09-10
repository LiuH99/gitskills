package com.wshuttle.trailerplatform.controller.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import com.wshuttle.trailerplatform.R;
import com.wshuttle.trailerplatform.amap.util.ToastUtil;
import com.wshuttle.trailerplatform.databinding.DialogMyOrderFilterBinding;
import com.wshuttle.trailerplatform.library.utils.LogUtils;
import com.wshuttle.trailerplatform.library.utils.StringUtils;
import com.wshuttle.trailerplatform.model.bean.CustomerDropRespEntity;
import com.wshuttle.trailerplatform.model.bean.EmpTreatEntity;
import com.wshuttle.trailerplatform.net.Response;
import com.wshuttle.trailerplatform.service.BaseApi;
import com.wshuttle.trailerplatform.service.TripApi;
import com.wshuttle.trailerplatform.util.JsonHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OrderFilterDialog extends DialogFragment implements View.OnClickListener {

    private DialogMyOrderFilterBinding binding;
    private OnFilterListener mListener;
    private int fragmentHeight;
    private String driverId;

    private String customerId;

    private String dispatchStatus;

    private long startTime;
    private long endTime;
    private String showStartTime;
    private String showEndTime;
    /**
     * 技师下拉框 相关
     */
    private AutoCompleteTextView spinnerDriver;
    private ArrayAdapter<EmpTreatEntity> adapter;
    private List<EmpTreatEntity> driverList;
    private EmpTreatEntity selectedDriver;
    /**
     * 客户下拉框 相关
     */
    private AutoCompleteTextView spinnerCustomer;
    private ArrayAdapter<CustomerDropRespEntity> customerAdapter;
    private List<CustomerDropRespEntity> customerList;
    private CustomerDropRespEntity selectedCustomer;

    /**
     * 时间选择相关
     */
    private DatePickerDialog datePickerDialog = null;

    public interface OnFilterListener {
        void onFilterApplied(String driver, String customer, String dispatchStatus, long startTime, long endTime);
    }

    public static OrderFilterDialog newInstance(int fragmentHeight, OnFilterListener listener) {
        OrderFilterDialog dialog = new OrderFilterDialog();
        Bundle args = new Bundle();
        args.putInt("DIALOG_HEIGHT", fragmentHeight);
        dialog.setArguments(args);
        dialog.mListener = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_Transparent);
        fragmentHeight = getArguments() != null ? getArguments().getInt("DIALOG_HEIGHT", -1) : -1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_my_order_filter, container, false);
        binding.setClick(this);
        // 初始化司机下拉框
        initDriverSpinner();
        // 初始化客户下拉框
        initCustomerSpinner();
        // 初始化时间
        startTime = endTime = System.currentTimeMillis();
        return binding.getRoot();
    }

    private void initCustomerSpinner() {
        // 初始化 AutoCompleteTextView（替代 Spinner）
        spinnerCustomer = binding.spinnerCustomer;
        customerList = new ArrayList<>();
        customerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, customerList);
        spinnerCustomer.setAdapter(customerAdapter);
        spinnerCustomer.setThreshold(1);
        // 技师下拉框接口
        new TripApi().customerDrop(new BaseApi() {
            @Override
            public void onSuccess(Response response, int id) {
                super.onSuccess(response, id);
                org.json.JSONArray jsonArray = response.getContent();
                List<CustomerDropRespEntity> newCustomerList = JsonHelper.parseArray(jsonArray.toString(), CustomerDropRespEntity.class);
                customerList.clear();
                customerList.addAll(newCustomerList);
                customerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response response, int id) {
                super.onError(response, id);
            }
        });
        spinnerCustomer.setOnFocusChangeListener((v, hasFocus) -> {
            spinnerCustomer.showDropDown();
        });
        // 点击就展开
        spinnerCustomer.setOnClickListener(v -> {
            spinnerCustomer.showDropDown();
        });
        // 监听选择事件
        spinnerCustomer.setOnItemClickListener((parent, view, position, id) -> {
            selectedCustomer = customerAdapter.getItem(position);
            Toast.makeText(getContext(), "选中客户: " + selectedCustomer.getName() + ", ID: " + selectedCustomer.getPartnerId(), Toast.LENGTH_SHORT).show();
        });
    }

    private void initDriverSpinner() {
        // 初始化 AutoCompleteTextView（替代 Spinner）
        spinnerDriver = binding.spinnerDriver;
        driverList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, driverList);
        spinnerDriver.setAdapter(adapter);
        spinnerDriver.setThreshold(1);
        // 技师下拉框接口
        new TripApi().getTech(new BaseApi() {
            @Override
            public void onSuccess(Response response, int id) {
                super.onSuccess(response, id);
                org.json.JSONArray jsonArray = response.getContent();
                List<EmpTreatEntity> newDriverList = JsonHelper.parseArray(jsonArray.toString(), EmpTreatEntity.class);
                driverList.clear();
                driverList.addAll(newDriverList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response response, int id) {
                super.onError(response, id);
            }
        });
        spinnerDriver.setOnFocusChangeListener((v, hasFocus) -> {
            spinnerDriver.showDropDown();
        });
        // 点击就展开
        spinnerDriver.setOnClickListener(v -> {
            spinnerDriver.showDropDown();
        });
        // 监听选择事件
        spinnerDriver.setOnItemClickListener((parent, view, position, id) -> {
            selectedDriver = adapter.getItem(position);
            Toast.makeText(getContext(), "选中司机: " + selectedDriver.getEmpName() + ", ID: " + selectedDriver.getEmpId(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // 获取屏幕宽度
                DisplayMetrics displayMetrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;

                // 设置宽度为屏幕的五分之四
                int dialogWidth = (int) (screenWidth * 4.0 / 5.0);

                // 设置窗口宽度为三分之二，高度为 Fragment 高度
                window.setLayout(dialogWidth, displayMetrics.heightPixels);
                window.setGravity(Gravity.RIGHT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // 设置背景变暗
                WindowManager.LayoutParams params = window.getAttributes();
                params.dimAmount = 0.5f;
                params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(params);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                resetFilters();
                break;
            case R.id.btn_confirm:
                applyFilters();
                dismiss();
                break;
            case R.id.btn_start_time:
                showTimePicker(new Date(startTime), true);
                break;
            case R.id.btn_end_time:
                showTimePicker(new Date(endTime), false);
                break;
        }
    }

    private void resetFilters() {
        // 重置筛选条件实现
    }

    private void applyFilters() {
        if (mListener != null) {
            // 获取筛选条件并回调
            mListener.onFilterApplied("", "", "", 0, 0); // 示例参数
        }
    }

    /**
     * 显示时间选择器
     * @param date 之前选择的时间
     * @param isStartTime 是否是开始时间
     */
    private void showTimePicker(Date date, boolean isStartTime) {
        // 创建 Calendar 实例并设置默认时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        
        // 创建日期选择器对话框
        datePickerDialog = new DatePickerDialog(
            getContext(),
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                    // 创建选择的日期
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, selectedYear);
                    selectedCalendar.set(Calendar.MONTH, selectedMonth);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    
                    if (isStartTime) {
                        // 开始时间：设置时分秒为 00:00:00
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        selectedCalendar.set(Calendar.MINUTE, 0);
                        selectedCalendar.set(Calendar.SECOND, 0);
                        selectedCalendar.set(Calendar.MILLISECOND, 0);
                        
                        startTime = selectedCalendar.getTimeInMillis();
                        showStartTime = StringUtils.formatDate(new Date(startTime), "yyyy-MM-dd");
                        
                        // 更新开始时间按钮文本
                        binding.btnStartTime.setText(showStartTime);
                        
                        LogUtils.d("OrderFilterDialog", "选择开始时间: " + showStartTime + " (时间戳: " + startTime + ")");
                        
                    } else {
                        // 结束时间：设置时分秒为 23:59:59
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, 23);
                        selectedCalendar.set(Calendar.MINUTE, 59);
                        selectedCalendar.set(Calendar.SECOND, 59);
                        selectedCalendar.set(Calendar.MILLISECOND, 999);
                        
                        endTime = selectedCalendar.getTimeInMillis();
                        showEndTime = StringUtils.formatDate(new Date(endTime), "yyyy-MM-dd");
                        
                        // 更新结束时间按钮文本
                        binding.btnEndTime.setText(showEndTime);
                        
                        LogUtils.d("OrderFilterDialog", "选择结束时间: " + showEndTime + " (时间戳: " + endTime + ")");
                    }
                }
            },
            year,  // 默认年份
            month, // 默认月份
            dayOfMonth // 默认日期
        );
        
        // 设置日期范围限制（可选）
        // 设置最小日期为 2020年1月1日
        Calendar minDate = Calendar.getInstance();
        minDate.set(2020, 0, 1);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        // 设置最大日期为 2030年12月31日
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(2030, 11, 31);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        // 显示日期选择器
        datePickerDialog.show();
    }
}