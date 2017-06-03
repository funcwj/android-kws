//
// Created by wujian on 16-12-28.
//

#include "signal.h"

short *FBANK_LOW_CHANS;
float *FBANK_LOW_WIGHT;

float frame[FRAME_WNDLEN + 1];
float fft[FRAME_FFTLEN + 1];


static int VectorSize(float* vec)
{
    return (int)(*vec);
}

static void ZeroVector(float* vec)
{
    memset(vec + 1, 0, sizeof(float) * VectorSize(vec));
}

static void SetVectorSize(float *vec, int sz)
{
    *vec = (float)sz;
}

void ZeroMeanFrame(float *src)
{
    float sum = 0.0;
    int vecSize = VectorSize(src);

    for (int i = 1; i <= vecSize; i++)
        sum += src[i];
    float avg = sum / vecSize;

    for (int i = 1; i <= vecSize; i++)
        src[i] -= avg;
}

float Mel(int k, float fres)
{
    return 1127 * log(1 + (k - 1) * fres);
}


static float* CreateVector(int size)
{
    float *buf = new float[size + 1];
    *buf = size;
    return buf;
}

static short* CreateShortVec(int size)
{
    short *buf = new short[size + 1];
    *buf = size;
    return buf;
}


void FFT(float *s, int invert)
{
    int ii, jj, n, nn, limit, m, j, inc, i;
    double wx, wr, wpr, wpi, wi, theta;
    double xre, xri, x;

    n = VectorSize(s);
    nn = n / 2; j = 1;
    // reverse
    for (ii = 1; ii <= nn; ii++) {
        i = 2 * ii - 1;
        if (j > i) {
            xre = s[j]; xri = s[j + 1];
            s[j] = s[i];  s[j + 1] = s[i + 1];
            s[i] = xre; s[i + 1] = xri;
        }
        m = n / 2;
        while (m >= 2  && j > m) {
            j -= m; m /= 2;
        }
        j += m;
    };

    limit = 2;
    while (limit < n) {
        inc = 2 * limit; theta = TPI / limit;
        if (invert) theta = -theta;
        x = sin(0.5 * theta);
        wpr = -2.0 * x * x; wpi = sin(theta);
        wr = 1.0; wi = 0.0;
        for (ii = 1; ii <= limit / 2; ii++) {
            m = 2 * ii - 1;
            for (jj = 0; jj <= (n - m) / inc; jj++) {
                i = m + jj * inc;
                j = i + limit;

                // (xre, xri) <= (wr, wi) * (s[j], s[j + 1])
                xre = wr * s[j] - wi * s[j + 1];
                xri = wr * s[j + 1] + wi * s[j];
                // (s[j], s[j + 1]) -= (wr, wi) * (s[j], s[j + 1])
                s[j] = s[i] - xre; s[j + 1] = s[i + 1] - xri;
                // (s[i], s[i + 1]) += (wr, wi) * (s[j], s[j + 1])
                s[i] = s[i] + xre; s[i + 1] = s[i + 1] + xri;
            }
            // (wr, wi) <= (wr, wi) * (cost, sint)
            // wr = wr * cost - wi * sint
            // wi = wi * cost + wr * sint
            wx = wr;
            wr = wr * wpr - wi * wpi + wr;
            wi = wi * wpr + wx * wpi + wi;
        }
        limit = inc;
    }
    if (invert)
        for (i = 1; i <= n;i++)
            s[i] = s[i] / nn;
}

void RealFFT(float* s)
{
    int n, n2, i, i1, i2, i3, i4;
    double xr1, xi1, xr2, xi2, wrs, wis;
    double yr, yi, yr2, yi2, yr0, theta, x;

    n = VectorSize(s) / 2; n2 = n / 2;
    theta = PI / n;
    FFT(s, 0);
    x = sin(0.5 * theta);
    // cos(PI / N) - 1
    yr2 = -2.0 * x * x;
    // sin(PI / N)
    yi2 = sin(theta);
    // cos(PI / N)
    yr = 1.0 + yr2;
    // sin(PI / N)
    yi = yi2;
    // 128
    for (i = 2; i <= n2; i++) {
        // (i1, i2): (3, 4)
        // (i3, i4): (511, 512)
        i1 = i + i - 1;      i2 = i1 + 1;
        i3 = n + n + 3 - i2; i4 = i3 + 1;
        wrs = yr;
        wis = yi;

        // xr1 - xi2 = s[i1]   xi1 + xr2 = s[i2]
        xr1 = (s[i1] + s[i3]) / 2.0; xi1 = (s[i2] - s[i4]) / 2.0;
        // xr1 + xi2 = s[i3]   xr2 - xi1 = s[i4]
        xr2 = (s[i2] + s[i4]) / 2.0; xi2 = (s[i3] - s[i1]) / 2.0;


        s[i1] = xr1 + wrs * xr2 - wis * xi2;
        s[i2] = xi1 + wrs * xi2 + wis * xr2;

        s[i3] = xr1 - wrs * xr2 + wis * xi2;
        s[i4] = -xi1 + wrs * xi2 + wis * xr2;

        // (yr, yi) += (yr, yi) * (yr2, yi2)
        yr0 = yr;
        yr = yr * yr2 - yi  * yi2 + yr;
        yi = yi * yr2 + yr0 * yi2 + yi;
    }
    xr1 = s[1];
    s[1] = xr1 + s[2];
    s[2] = 0.0;
}

void PreEmphasise(float* s, float preE)
{
    for (int i = VectorSize(s); i >= 2; i--)
        s[i] -= s[i - 1] * preE;
    s[1] *= 1.0 - preE;
}


// s[2] -= s[1] * preE

void Ham(float *s, float *ham)
{
    int frameSize = VectorSize(s);
    float a = TPI / (frameSize - 1);

    for (int i = 1; i <= frameSize; i++)
        ham == NULL ? s[i] = s[i] * (0.54 - 0.46 * cos(a * (i - 1))): s[i] = s[i] * ham[i];
}

// OK
void InitSignalProc()
{
    LOGI("InitSignalProc...");

    SetVectorSize(fft, FRAME_FFTLEN);
    SetVectorSize(frame, FRAME_WNDLEN);

    int maxChan = FBANK_MAXCHS + 1, Nby2 = FRAME_FFTLEN >> 1;

    float fres = 1.0E7 / (625 * FRAME_FFTLEN * 700);
    float mlo = 0, mhi = Mel(Nby2 + 1, fres), ms = mhi - mlo;

    float *cf = CreateVector(maxChan);

    // cf => [mlo, mhi]
    for(int i = 1; i <= maxChan; i++)
        cf[i] = ((float)i / (float)maxChan) * ms + mlo;

    int chan = 1;
    FBANK_LOW_CHANS = CreateShortVec(Nby2);
    FBANK_LOW_CHANS[1] = -1;

    for(int i = 2; i <= Nby2; i++)
    {
        float m = Mel(i, fres);
        while(cf[chan] < m && chan <= maxChan)
            chan++;
        FBANK_LOW_CHANS[i] = chan - 1;
    }

    FBANK_LOW_WIGHT = CreateVector(Nby2);
    FBANK_LOW_WIGHT[1] = 0.0;

    for(int i = 2; i <= Nby2; i++)
    {
        chan = FBANK_LOW_CHANS[i];

        if(chan > 0)
            FBANK_LOW_WIGHT[i] = (cf[chan + 1] - Mel(i, fres)) / (cf[chan + 1] - cf[chan]);
        else
            FBANK_LOW_WIGHT[i] = (cf[1] - Mel(i, fres)) / (cf[1] - mlo);
    }

    delete[] cf;

}

void DeposeSignalProc()
{
    LOGI("DeposeSignalProc...");
    if (FBANK_LOW_CHANS != NULL)
        delete[] FBANK_LOW_CHANS;
    if (FBANK_LOW_WIGHT != NULL)
        delete[] FBANK_LOW_WIGHT;
}

float* WaveToFBank(float *wave)
{
    if(FBANK_LOW_CHANS == NULL || FBANK_LOW_WIGHT == NULL)
    {
        KWS_ERROR("Part of DSP not initialized");
        return NULL;
    }
    int pos = 1, bin, cnt = 0;

    int nframes = (VectorSize(wave) - FRAME_WNDLEN) / FRAME_OFFSET + 1;
    float *fbank = CreateVector(FBANK_MAXCHS);
    float *wndow = CreateVector(FBANK_MAXCHS * nframes);

    float s1, s2, s, t;

    while(cnt <= nframes)
    {

        for(int i = 1; i <= VectorSize(frame); i++)
            frame[i] = wave[i + pos - 1];

        pos += FRAME_OFFSET;

        ZeroMeanFrame(frame);
        PreEmphasise(frame, EMP_COFX);
        Ham(frame, NULL);

        ZeroVector(fft);
        for(int i = 1; i <= VectorSize(frame); i++)
            fft[i] = frame[i];
        RealFFT(fft);

        ZeroVector(fbank);

        for(int i = 2; i <= FRAME_FFTLEN / 2; i++)
        {
            s1 = fft[i * 2 - 1], s2 = fft[i * 2];
            s  = sqrt(s1 * s1 + s2 * s2);
            bin = FBANK_LOW_CHANS[i];
            t  =  FBANK_LOW_WIGHT[i] * s;
            if(bin > 0)
                fbank[bin] += t;
            if(bin < FBANK_MAXCHS)
                fbank[bin + 1] += s - t;
        }
        for(int i = 1; i <= VectorSize(fbank); i++) {
            fbank[i] = fbank[i] < 1.0 ? 0 : log(fbank[i]);
            fbank[i] -= fbank_cm[i - 1];
            fbank[i] /= sqrt(fbank_cv[i - 1]);
        }

        memcpy(wndow + 1 + cnt * FBANK_MAXCHS, fbank + 1, VectorSize(fbank) * sizeof(float));
        cnt++;
    }

    delete[] fbank;
    return wndow;
}
