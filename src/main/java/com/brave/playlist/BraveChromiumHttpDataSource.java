/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist;

import static com.google.android.exoplayer2.upstream.HttpUtil.buildRangeRequestHeader;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;
import static com.google.android.exoplayer2.util.Util.castNonNull;
import static java.lang.Math.min;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceException;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.HttpUtil;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BraveChromiumHttpDataSource implements HttpDataSource {

    /**
     * The default connection timeout, in milliseconds.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 8 * 1000;
    /**
     * The default read timeout, in milliseconds.
     */
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 8 * 1000;
    public static ByteArrayOutputStream output = new ByteArrayOutputStream();
    private static final String TAG = "DataSource";
    private static final long MAX_BYTES_TO_DRAIN = 2048;
    private final RequestProperties requestProperties = new RequestProperties();
    @Nullable
    private Predicate<String> contentTypePredicate;
    @Nullable
    private DataSpec dataSpec;
    @Nullable
    private HttpURLConnection connection;
    @Nullable
    private InputStream inputStream;
    private boolean opened;
    private int responseCode;
    private long bytesToRead;
    private long bytesRead;

    @Override
    @Nullable
    public Uri getUri() {
        return connection == null ? null : Uri.parse(connection.getURL().toString());
//        return Uri.parse("https://rr3---sn-8qu-t0ay.googlevideo.com/videoplayback?expire=1690003651&ei=YxS7ZIS5D66m_9EPuqK1-AY&ip=23.233.146.226&id=o-AFsOduPdB3ssYUdvv8n3TW97MOnYDMfxOu_RhA-AjaAh&itag=18&source=youtube&requiressl=yes&mh=XQ&mm=31%2C29&mn=sn-8qu-t0ay%2Csn-t0a7lnee&ms=au%2Crdu&mv=m&mvi=3&pl=18&initcwndbps=1890000&spc=Ul2Sq8zDRTOvbxmQDYMM6rLEimM_40E_6kAuWtmyKg&vprv=1&svpuc=1&mime=video%2Fmp4&ns=meRQFhHWvyhqEAtzOv79DxUO&cnr=14&ratebypass=yes&dur=238.886&lmt=1665537626810673&mt=1689981652&fvip=4&fexp=24007246%2C24363391&c=MWEB&txp=5538434&n=KMLXonjnMJ8CQA&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Csvpuc%2Cmime%2Cns%2Ccnr%2Cratebypass%2Cdur%2Clmt&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRAIgfgmHTUSKJ8XTBD8psA3MNOm4lg8ylHVrtALq2A9hTK4CIHEhNw8hMZIuwgdurXJ1VuJBQZ3udB0oRX0k9pz7rL8E&sig=AOq0QJ8wRQIhAIGt9dHymHA8_5uVPPCCUyf9AQn_Fh3NtjerpG1KHczrAiB91fq9OxO8nz20EQj-VGpOw9hAaMLtwuqiP9my_q6XIQ%3D%3D&cpn=2qlKtO6c01-mUGMP&cver=2.20230720.05.00&ptk=youtube_multi&oid=YbGWGCIUCoaUePqGy_acMw.yfdyTRzMmu3FPmmTBoa5Wg&pltype=contentugc");
    }

    @Override
    public int getResponseCode() {
//        return connection == null || responseCode <= 0 ? -1 : responseCode;
        return 200;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        if (connection == null) {
            return ImmutableMap.of();
        }
        return connection.getHeaderFields();
    }

    @Override
    public void setRequestProperty(String name, String value) {
        checkNotNull(name);
        checkNotNull(value);
        requestProperties.set(name, value);
    }

    @Override
    public void clearRequestProperty(String name) {
        checkNotNull(name);
        requestProperties.remove(name);
    }

    @Override
    public void clearAllRequestProperties() {
        requestProperties.clear();
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {

    }

    /**
     * Opens the source to read the specified data.
     */
    @Override
    public long open(DataSpec dataSpec) throws HttpDataSourceException {
        this.dataSpec = dataSpec;
        Log.e("NTP", "open : dataspec : " + dataSpec.toString());
        bytesRead = 0;
        bytesToRead = 0;
//        transferInitializing(dataSpec);

        String responseMessage;
        HttpURLConnection connection;
        try {
            this.connection = makeConnection(dataSpec);
            connection = this.connection;
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException e) {
            closeConnectionQuietly();
            throw HttpDataSourceException.createForIOException(
                    e, dataSpec, HttpDataSourceException.TYPE_OPEN);
        }

        // Check for a valid response code.
        if (responseCode < 200 || responseCode > 299) {
            Map<String, List<String>> headers = connection.getHeaderFields();
            if (responseCode == 416) {
                long documentSize =
                        HttpUtil.getDocumentSize(connection.getHeaderField(HttpHeaders.CONTENT_RANGE));
                if (dataSpec.position == documentSize) {
                    opened = true;
//                    transferStarted(dataSpec);
                    return dataSpec.length != C.LENGTH_UNSET ? dataSpec.length : 0;
                }
            }

            @Nullable InputStream errorStream = connection.getErrorStream();
            byte[] errorResponseBody;
            try {
                errorResponseBody =
                        errorStream != null ? Util.toByteArray(errorStream) : Util.EMPTY_BYTE_ARRAY;
            } catch (IOException e) {
                errorResponseBody = Util.EMPTY_BYTE_ARRAY;
            }
            closeConnectionQuietly();
            @Nullable
            IOException cause =
                    responseCode == 416
                            ? new DataSourceException(PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE)
                            : null;
            throw new InvalidResponseCodeException(
                    responseCode, responseMessage, cause, headers, dataSpec, errorResponseBody);
        }

        // Check for a valid content type.
        String contentType = connection.getContentType();
        if (contentTypePredicate != null && !contentTypePredicate.apply(contentType)) {
            closeConnectionQuietly();
            throw new InvalidContentTypeException(contentType, dataSpec);
        }

        if (dataSpec.length != C.LENGTH_UNSET) {
            bytesToRead = dataSpec.length;
        } else {
            long contentLength =
                    HttpUtil.getContentLength(
                            connection.getHeaderField(HttpHeaders.CONTENT_LENGTH),
                            connection.getHeaderField(HttpHeaders.CONTENT_RANGE));
            bytesToRead =
                    (contentLength);
        }

        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            closeConnectionQuietly();
            throw new HttpDataSourceException(
                    e,
                    dataSpec,
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                    HttpDataSourceException.TYPE_OPEN);
        }

        opened = true;

        return bytesToRead;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int length) throws HttpDataSourceException {
        try {
            return readInternal(buffer, offset, length);
        } catch (IOException e) {
            throw HttpDataSourceException.createForIOException(
                    e, castNonNull(dataSpec), HttpDataSourceException.TYPE_READ);
        }
    }

    @Override
    public void close() throws HttpDataSourceException {
        try {
            @Nullable InputStream inputStream = this.inputStream;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new HttpDataSourceException(
                            e,
                            castNonNull(dataSpec),
                            PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                            HttpDataSourceException.TYPE_CLOSE);
                }
            }
        } finally {
            inputStream = null;
            closeConnectionQuietly();
            if (opened) {
                opened = false;
            }
        }
    }

    /**
     * Establishes a connection, following redirects to do so where permitted.
     */
    private HttpURLConnection makeConnection(DataSpec dataSpec) throws IOException {
        URL url = new URL(dataSpec.uri.toString());
        @DataSpec.HttpMethod int httpMethod = dataSpec.httpMethod;
        @Nullable byte[] httpBody = dataSpec.httpBody;
        long position = dataSpec.position;
        long length = dataSpec.length;
        return makeConnection(
                url,
                httpMethod,
                httpBody,
                position,
                length,
                dataSpec.httpRequestHeaders);
    }

    /**
     * Configures a connection and opens it.
     *
     * @param url               The url to connect to.
     * @param httpMethod        The http method.
     * @param httpBody          The body data, or {@code null} if not required.
     * @param position          The byte offset of the requested data.
     * @param length            The length of the requested data, or {@link C#LENGTH_UNSET}.
     * @param requestParameters parameters (HTTP headers) to include in request.
     */
    private HttpURLConnection makeConnection(
            URL url,
            @DataSpec.HttpMethod int httpMethod,
            @Nullable byte[] httpBody,
            long position,
            long length,
            Map<String, String> requestParameters)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setConnectTimeout(connectTimeoutMillis);
//        connection.setReadTimeout(readTimeoutMillis);

        Map<String, String> requestHeaders = new HashMap<>();
//        if (defaultRequestProperties != null) {
//            requestHeaders.putAll(defaultRequestProperties.getSnapshot());
//        }
        requestHeaders.putAll(requestProperties.getSnapshot());
        requestHeaders.putAll(requestParameters);

        for (Map.Entry<String, String> property : requestHeaders.entrySet()) {
            connection.setRequestProperty(property.getKey(), property.getValue());
        }

        @Nullable String rangeHeader = buildRangeRequestHeader(position, length);
        if (rangeHeader != null) {
            connection.setRequestProperty(HttpHeaders.RANGE, rangeHeader);
        }
//        if (userAgent != null) {
//            connection.setRequestProperty(HttpHeaders.USER_AGENT, userAgent);
//        }
        connection.setDoOutput(httpBody != null);
        connection.setRequestMethod(DataSpec.getStringForHttpMethod(httpMethod));

        if (httpBody != null) {
            connection.setFixedLengthStreamingMode(httpBody.length);
            connection.connect();
            OutputStream os = connection.getOutputStream();
            os.write(httpBody);
            os.close();
        } else {
            connection.connect();
        }
        return connection;
    }

    /**
     * Reads up to {@code length} bytes of data and stores them into {@code buffer}, starting at index
     * {@code offset}.
     *
     * <p>This method blocks until at least one byte of data can be read, the end of the opened range
     * is detected, or an exception is thrown.
     *
     * @param buffer     The buffer into which the read data should be stored.
     * @param offset     The start offset into {@code buffer} at which data should be written.
     * @param readLength The maximum number of bytes to read.
     * @return The number of bytes read, or {@link C#RESULT_END_OF_INPUT} if the end of the opened
     * range is reached.
     * @throws IOException If an error occurs reading from the source.
     */
    private int readInternal(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        }

        Log.e("NTP", "readInternal");
//        if (bytesToRead != C.LENGTH_UNSET) {
//            long bytesRemaining = bytesToRead - bytesRead;
//            if (bytesRemaining == 0) {
//                return C.RESULT_END_OF_INPUT;
//            }
//            readLength = (int) min(readLength, bytesRemaining);
//        }
        readLength = min(readLength, 200);

        int read = castNonNull(inputStream).read(buffer, offset, readLength);
        if (read == -1) {
            return C.RESULT_END_OF_INPUT;
        }

//        bytesRead += read;
        return read;
    }

    /**
     * Closes the current connection quietly, if there is one.
     */
    private void closeConnectionQuietly() {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while disconnecting", e);
            }
            connection = null;
        }
    }

    /**
     * {@link DataSource.Factory} for {@link BraveChromiumHttpDataSource} instances.
     */
    public static final class Factory implements HttpDataSource.Factory {

        /**
         * Creates an instance.
         */
        public Factory() {
        }

        @Override
        public BraveChromiumHttpDataSource.Factory setDefaultRequestProperties(Map<String, String> defaultRequestProperties) {
            return this;
        }

        @Override
        public BraveChromiumHttpDataSource createDataSource() {
            BraveChromiumHttpDataSource dataSource =
                    new BraveChromiumHttpDataSource();
            return dataSource;
        }
    }
}
