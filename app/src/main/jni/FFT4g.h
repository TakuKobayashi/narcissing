class FFT4g {
public:
    FFT4g(int n);
    virtual ~FFT4g();
    void rdft(int, double[]);
private:
    int ip[];
    double w[];
    int n;
};
