package ru.idealplm.vsp.oceanos.xml;

import java.io.InputStream;

import ru.idealplm.vsp.oceanos.core.ReportConfiguration;

public class PDFReportBuilderConfiguration extends ReportConfiguration
{
	private InputStream templateStream;
	private InputStream configStream;

	public PDFReportBuilderConfiguration(InputStream templateStream, InputStream configStream)
	{
		setTemplateStream(templateStream);
		setConfigStream(configStream);
	}

	public void setTemplateStream(InputStream templateStream)
	{
		this.templateStream = templateStream;
	}

	public InputStream getTemplateStream()
	{
		if (this.templateStream != null) {
			return this.templateStream;
		} else {
			return getDefaultTemplateStream();
		}
	}

	public void setConfigStream(InputStream configStream)
	{
		this.configStream = configStream;
	}

	public InputStream getConfigStream()
	{
		if (this.configStream != null) {
			return this.configStream;
		} else {
			return getDefaultConfigStream();
		}
	}

	public InputStream getDefaultTemplateStream()
	{
		System.err.println("Using default template file.");
		return PDFReportBuilderConfiguration.class.getResourceAsStream("/pdf/DefaultSpecPDFTemplate.xsl");
	}

	public InputStream getDefaultConfigStream()
	{
		System.err.println("Using default config file.");
		return PDFReportBuilderConfiguration.class.getResourceAsStream("/pdf/DefaultSpecPDFUserconfig.xml");
	}
}
