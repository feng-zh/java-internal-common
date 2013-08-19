package com.hp.ts.rnd.tool.perf.web;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import com.hp.ts.rnd.tool.perf.org.apache.coyote.http11.filters.FlushableGZIPOutputStream;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
class GZIPEncodingFilter extends com.sun.net.httpserver.Filter {

	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
		Headers headers = exchange.getRequestHeaders();
		if (headers.containsKey("Accept-Encoding")
				&& headers.getFirst("Accept-Encoding").contains("gzip")) {
			// gzip required
			exchange.getResponseHeaders().add("Content-Encoding", "gzip");
			exchange.setStreams(
					exchange.getRequestBody(),
					new BufferedOutputStream(new FilterOutputStream(exchange
							.getResponseBody()) {

						@Override
						public void write(int paramInt) throws IOException {
							setGzipStream();
							super.write(paramInt);
						}

						@Override
						public void write(byte[] paramArrayOfByte,
								int paramInt1, int paramInt2)
								throws IOException {
							if (paramInt2 > 0) {
								setGzipStream();
							}
							super.write(paramArrayOfByte, paramInt1, paramInt2);
						}

						private void setGzipStream() throws IOException {
							if (!(out instanceof FlushableGZIPOutputStream)) {
								out = new FlushableGZIPOutputStream(out);
							}
						}

					}));
		}
		chain.doFilter(exchange);
	}

	@Override
	public String description() {
		return "GZIP Encoding Filter";
	}

}
