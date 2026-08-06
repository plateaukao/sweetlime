"""Synthesize a short piano-like C4 note as a 16-bit mono WAV.

Additive synthesis: 8 harmonics with per-harmonic decay (higher partials die
faster), slight string inharmonicity, fast attack. Public-domain output.
"""
import math
import struct
import wave

SR = 44100
F0 = 261.63  # C4 = "do"
DUR = 0.30
N = int(SR * DUR)

# harmonic amplitudes roughly following a piano spectrum
AMPS = [1.00, 0.62, 0.38, 0.26, 0.16, 0.10, 0.07, 0.05]
B = 0.00015  # inharmonicity coefficient

samples = []
for i in range(N):
    t = i / SR
    v = 0.0
    for n, a in enumerate(AMPS, start=1):
        fn = n * F0 * math.sqrt(1.0 + B * n * n)
        tau = 0.12 / (n ** 0.7)  # higher harmonics decay faster
        v += a * math.exp(-t / tau) * math.sin(2 * math.pi * fn * t)
    # overall envelope: 4 ms attack, gentle release at the tail
    env = min(1.0, t / 0.004)
    tail = min(1.0, (DUR - t) / 0.02)
    samples.append(v * env * tail)

peak = max(abs(s) for s in samples)
scale = 0.5 * 32767 / peak
data = b"".join(struct.pack("<h", int(s * scale)) for s in samples)

out = "/Users/maoyuankao/src/sweetlime/LimeStudio/app/src/main/res/raw/piano_c4.wav"
with wave.open(out, "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(data)
print("wrote", out, len(data) + 44, "bytes")
