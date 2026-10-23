# Streaming Feasibility and Known Limitations

## Go/No-Go Gate

**Do not proceed to full UI implementation until streaming behavior is measured.** This is the highest-risk part of the project.

## Telegram File Streaming Model

A Telegram file is **not** automatically a regular HTTP URL. Media3 reads bytes through a `DataSource`; TDLib exposes file-management and download operations. The bridge between these interfaces is the critical implementation detail.

### Available Approaches

| Approach | Description | Pros | Cons |
|----------|-------------|------|------|
| **TDLib-managed download** | TDLib downloads the file to a local temp path; `DataSource` reads from the file | - Reliable random-access<br>- Full seeking support<br>- Progress tracking via TDLib | - Additional download step before playback<br>- Double storage (temp + cache)<br>- Network dependency for startup> |
| **Local-file bridge over growing download** | Small local file that grows as ExoPlayer reads; `RandomAccessFile` bridge | - No pre-download wait<br>- Supports seeking as file grows<br>- Bounded by cache limits | - Complex file locking<br>- Potential race conditions with TDLib<br>- Not all DataSource implementations support> |
| **Separate streaming service** | External service (e.g., real-debrid, cloudstream) provides HTTP URLs | - Best streaming performance<br>- Offloads download/transcoding<br>- Can provide direct URLs | - Requires third-party service account<br>- May not respect user's Telegram privacy<br>- Additional cost/service dependency> |

## Prototype Recommendation

**Start with TDLib-managed download to temp file** for the proof-of-concept. This gives:

1. Reliable random-access reads for seeking
2. Transparent progress reporting
3. Simple implementation using existing TDLib APIs
4. Ability to measure real-world startup delay, buffering, memory, and disk usage

### Metrics to Measure

- **Startup delay:** Time from user selection to first frame
- **Buffering events:** Count and duration of re-buffering during playback
- **Memory usage:** ExoPlayer + TDLib heap allocation
- **Disk usage:** Temp file size, Room cache size, total growth over time
- **Seeking accuracy:** Time to seek to arbitrary positions; accuracy of position resume
- **Recovery:** Behavior when network drops mid-playback; ability to resume

### Decision Points

If TDLib cannot provide reliable incremental/random-access reads, compare:

1. **Continue with TDLib download** — accept the pre-download wait but get seeking support
2. **Switch to local-file bridge** — attempt growing file approach if the DataSource supports it
3. **Integrate separate streaming service** — if user is willing to connect third-party accounts

### Known Streaming Limitations

- **No guaranteed seek support** until file is fully downloaded or bridge is validated
- **Codec variability** — playability depends on TV hardware; some formats may fail silently
- **Subtitle support** — TDLib exposes subtitle data; ExoPlayer integration needs validation
- **Audio track selection** — multiple audio tracks within one video may not be switchable
- **Network interruption** — playback may stall; recovery logic needs explicit testing
- **File size limits** — very large videos may fill temp storage or exceed cache quotas

### Next Steps for Streaming POC

1. Create minimal Media3 player screen
2. Implement prototype Telegram-backed `DataSource` or validated local-file bridge
3. Measure the metrics listed above on both emulator and physical TV
4. Test seeking, cancellation, interrupted connectivity and recovery
5. Use results to decide: stay with TDLib download, switch approaches, or add service integration