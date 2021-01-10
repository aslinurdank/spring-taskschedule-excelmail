package com.suonic.util;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import com.suonic.service.ScheduleDbService;

public class MailTask {
	private static Logger LOGGER = LoggerFactory
			.getLogger(MailTask.class);
	private ScheduleDbService scheduleDbService;

	public ScheduleDbService getScheduleDbService() {
		return scheduleDbService;
	}

	public void setScheduleDbService(ScheduleDbService scheduleDbService) {
		this.scheduleDbService = scheduleDbService;
	}

	private MailSender mailSender;

	public MailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	// @Scheduled(cron = "*/5 * * * * *")
	// @Scheduled(cron = "0 0 12 2 * ?") //Every month on the 2nd, at noon
	// @Scheduled(cron = "0 */10 * ? * *") // Every 10 minutes
	// @Scheduled(cron = "0 0 12 1 * ?") // Every month on the 1nd, at noon
	@Scheduled(cron = "0 0 8 * * 1-5") // Every weekday at 8:00
	// @Scheduled(cron = "*/20 * * * * *")// Every 10 seconds
	public void runTask() throws Exception {
		final List<Map<String, Object>> mailList = scheduleDbService.getMailEmployee();

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar takvim = Calendar.getInstance();

		if (takvim.get(takvim.DAY_OF_WEEK) != Calendar.SUNDAY && takvim.get(takvim.DAY_OF_WEEK) != Calendar.SATURDAY) {
			try {
				System.out.println("Schedule is starting:--" + new Date().toString());

				StringBuilder mesaj = new StringBuilder();

				for (Map employee : mailList) {

					if (employee != null) {
						List<Map<String, Object>> getIOList = null;
						getIOList = scheduleDbService.getIO(Long.valueOf(employee.get("USERID").toString()));
						if (getIOList != null) {
							InputStream in = print(mailList);
							mailSender.sendMail(employee.get("E_MAIL").toString(),"", in,
									"Mail List");
						}
					}

				}

				System.out.println("Schedule is over:--" + new Date().toString());

			} catch (Exception e1) {
				System.out.println("runTask error:" + e1.toString());
			}
		}
	}

	public InputStream print(List<Map<String, Object>> liste) throws ColumnBuilderException, ClassNotFoundException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		ByteArrayOutputStream output = null;
		FastReportBuilder drb = new FastReportBuilder();
		DynamicReport dr;
		Style columnHeader = new Style();
		Font myFont = new Font(7, "DejaVu Sans", true);
		Font myFont2 = new Font(8, "DejaVu Sans", false);
		Font myFont3 = new Font(12, "DejaVu Sans", true);
		columnHeader.setBackgroundColor(Color.white);
		columnHeader.setFont(myFont);
		Style columnDetail = new Style();
		columnDetail.setFont(myFont2);
		InputStream mediais;
		Style titleDetail = new Style();
		titleDetail.setHorizontalAlign(HorizontalAlign.LEFT);
		titleDetail.setFont(myFont3);
		drb.setTitleStyle(titleDetail);

		drb.setTitle("Mail Report");
		drb.addColumn("Full Name", "FULL_NAME", String.class.getName(), 50, true);
		drb.addColumn("Average Working Time", "AVERAGE_WRKTIME", String.class.getName(), 150, true);

		Page pg = new Page(4000, 3500, false);
		drb.setPageSizeAndOrientation(pg);
		drb.setPrintBackgroundOnOddRows(false);
		drb.setPrintColumnNames(true);
		drb.setIgnorePagination(true);
		drb.setDefaultStyles(null, null, columnHeader, columnDetail);
		drb.setUseFullPageWidth(true);
		try {
			dr = drb.build();
			JRDataSource ds = new JRBeanCollectionDataSource(liste);
			JasperReport report = null;

			report = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), new HashMap());

			output = new ByteArrayOutputStream();
			JasperPrint jasperPrint = null;

			jasperPrint = JasperFillManager.fillReport(report, new HashMap(), ds);

			JRXlsExporter exporterXLS = new JRXlsExporter();
			exporterXLS.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);

			exporterXLS.setParameter(JRExporterParameter.OUTPUT_STREAM, output);
			exporterXLS.setParameter(JRXlsAbstractExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsAbstractExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsAbstractExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);

			exporterXLS.exportReport();

			output.flush();
			output.close();

		} catch (Exception e) {
			LOGGER.error("MailTask  print error:", e);
		}
		return new ByteArrayInputStream(output.toByteArray());

	}

}
