import { useState } from 'react'
import { Metric } from '../components/Metric.jsx'
import { RuleInput } from '../components/RuleInput.jsx'
import { useCaptureProcessing } from '../hooks/useCaptureProcessing.js'
import './InspectorPage.css'

const applications = {
  UNKNOWN: 'Unknown', HTTP: 'HTTP', HTTPS: 'HTTPS', DNS: 'DNS', TLS: 'TLS', QUIC: 'QUIC',
  GOOGLE: 'Google', FACEBOOK: 'Facebook', YOUTUBE: 'YouTube', TWITTER: 'Twitter/X',
  INSTAGRAM: 'Instagram', NETFLIX: 'Netflix', AMAZON: 'Amazon', MICROSOFT: 'Microsoft',
  APPLE: 'Apple', WHATSAPP: 'WhatsApp', TELEGRAM: 'Telegram', TIKTOK: 'TikTok',
  SPOTIFY: 'Spotify', ZOOM: 'Zoom', DISCORD: 'Discord', GITHUB: 'GitHub',
  CLOUDFLARE: 'Cloudflare',
}
const ruleLabels = {
  blockedIps: 'Blocked IPs',
  blockedApps: 'Blocked apps',
  blockedDomains: 'Blocked domains',
  blockedPorts: 'Blocked ports',
}

const parseRules = (value) => value.split(/[,\n]/).map((entry) => entry.trim()).filter(Boolean)

function InspectorPage() {
  const [file, setFile] = useState(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [blockIps, setBlockIps] = useState('')
  const [blockApps, setBlockApps] = useState('')
  const [blockDomains, setBlockDomains] = useState('')
  const [blockPorts, setBlockPorts] = useState('')
  const [dragging, setDragging] = useState(false)
  const { busy, error, report, downloadUrl, run, reset, setError } = useCaptureProcessing()

  function chooseFile(nextFile) {
    if (!nextFile) return
    setFile(nextFile)
    reset()
  }

  async function submit(event) {
    event.preventDefault()
    if (!file || !username || !password) {
      setError('Choose a capture and enter your API credentials to continue.')
      return
    }

    await run(file, {
      blockIp: parseRules(blockIps),
      blockApp: parseRules(blockApps),
      blockDomain: parseRules(blockDomains),
      blockPort: parseRules(blockPorts),
    }, username, password)
  }

  const total = report?.totalPackets || 0
  const dropRate = total ? (report.droppedPackets / total) * 100 : 0
  const appEntries = Object.entries(report?.applications || {})
    .sort((left, right) => right[1] - left[1])
    .filter(([, count]) => count > 0)
  const domainEntries = Object.entries(report?.topDomains || {})
    .sort((left, right) => right[1] - left[1])
    .slice(0, 5)
  const ruleEntries = Object.entries(report?.rules || {})
    .filter(([, count]) => count > 0)

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="Packet Inspector home">
          <span className="brand-mark"><span /></span>
          <span>packet<span className="brand-light">inspector</span></span>
        </a>
        <div className="local-badge"><span /> LOCAL ANALYSIS</div>
      </header>

      <main>
        <section className="intro">
          <div className="eyebrow"><span className="eyebrow-line" /> DEEP PACKET INSPECTION</div>
          <h1>See what’s moving<br /><span>through your network.</span></h1>
          <p>Inspect a PCAP, apply precise traffic rules, and export a clean filtered capture.</p>
        </section>

        <form className="workspace" onSubmit={submit}>
          <section className="panel capture-panel">
            <div className="section-heading">
              <div className="section-number">01</div>
              <div>
                <h2>Capture file</h2>
                <p>Choose an Ethernet PCAP to inspect</p>
              </div>
            </div>
            <label
              className={`drop-zone${dragging ? ' is-dragging' : ''}${file ? ' has-file' : ''}`}
              onDragOver={(event) => { event.preventDefault(); setDragging(true) }}
              onDragLeave={() => setDragging(false)}
              onDrop={(event) => {
                event.preventDefault()
                setDragging(false)
                chooseFile(event.dataTransfer.files[0])
              }}
            >
              <input
                type="file"
                accept=".pcap,.cap,application/vnd.tcpdump.pcap"
                onChange={(event) => chooseFile(event.target.files[0])}
              />
              <span className="file-icon" aria-hidden="true">
                {file ? <CheckIcon /> : <UploadIcon />}
              </span>
              <strong>{file ? file.name : 'Drop your capture here'}</strong>
              <span className="drop-subtitle">{file ? formatBytes(file.size) : 'or browse files · PCAP / CAP'}</span>
              {!file && <span className="browse-button">Browse files</span>}
              {file && <span className="replace-hint">Click to replace capture</span>}
            </label>

            <div className="auth-row">
              <div className="auth-symbol"><LockIcon /></div>
              <div className="auth-fields">
                <div className="auth-label">
                  <strong>API credentials</strong>
                  <span>HTTP Basic · credentials stay in this tab</span>
                </div>
                <div className="credential-inputs">
                  <input aria-label="Username" autoComplete="username" placeholder="Username"
                    value={username} onChange={(event) => setUsername(event.target.value)} />
                  <input aria-label="Password" autoComplete="current-password" placeholder="Password"
                    type="password" value={password}
                    onChange={(event) => setPassword(event.target.value)} />
                </div>
              </div>
            </div>
          </section>

          <section className="panel rules-panel">
            <div className="section-heading">
              <div className="section-number">02</div>
              <div>
                <h2>Blocking rules</h2>
                <p>Optional · one entry per line or comma-separated</p>
              </div>
              <span className="optional-tag">OPTIONAL</span>
            </div>
            <div className="rule-grid">
              <RuleInput label="Source IPs" hint="192.168.1.25" value={blockIps} onChange={setBlockIps} />
              <RuleInput label="Applications" hint="YouTube, TikTok" value={blockApps} onChange={setBlockApps} />
              <RuleInput label="Domains" hint="*.example.com" value={blockDomains} onChange={setBlockDomains} />
              <RuleInput label="Destination ports" hint="80, 443, 8080" value={blockPorts} onChange={setBlockPorts} />
            </div>
            <div className="rule-note"><InfoIcon /> Rules are applied to source IP, destination port, application, and domain matches.</div>
          </section>

          <div className="action-row">
            <span className="format-note"><span className="format-dot" /> IPv4 · TCP / UDP · classic PCAP</span>
            <button className="process-button" type="submit" disabled={busy}>
              {busy ? <><span className="spinner" /> Inspecting capture…</> : <>Inspect traffic <ArrowIcon /></>}
            </button>
          </div>
        </form>

        {error && <div className="error-banner" role="alert"><span>!</span>{error}</div>}

        {report && (
          <section className="report-panel" aria-live="polite">
            <div className="report-header">
              <div>
                <div className="eyebrow"><span className="eyebrow-line" /> ANALYSIS COMPLETE</div>
                <h2>Traffic report</h2>
              </div>
              <a className="download-button" href={downloadUrl} download="filtered.pcap">
                <DownloadIcon /> Download filtered PCAP
              </a>
            </div>

            <div className="metric-grid">
              <Metric label="Packets inspected" value={formatNumber(report.totalPackets)} />
              <Metric label="Forwarded" value={formatNumber(report.forwardedPackets)} tone="green" />
              <Metric label="Blocked" value={formatNumber(report.droppedPackets)} tone="red" />
              <Metric label="Data inspected" value={formatBytes(report.totalBytes)} />
            </div>

            <div className="report-details">
              <div className="detail-card">
                <div className="detail-title">FILTERING SUMMARY <span>{dropRate.toFixed(1)}% blocked</span></div>
                <div className="traffic-bar"><span style={{ width: `${100 - dropRate}%` }} /><i style={{ width: `${dropRate}%` }} /></div>
                <div className="legend">
                  <span><i className="legend-forwarded" />Forwarded <b>{formatNumber(report.forwardedPackets)}</b></span>
                  <span><i className="legend-blocked" />Blocked <b>{formatNumber(report.droppedPackets)}</b></span>
                </div>
                <div className="protocol-row">
                  <span>TCP <b>{formatNumber(report.tcpPackets)}</b></span>
                  <span>UDP <b>{formatNumber(report.udpPackets)}</b></span>
                  <span>Active flows <b>{formatNumber(report.activeConnections)}</b></span>
                </div>
              </div>
              <div className="detail-card app-card">
                <div className="detail-title">APPLICATIONS DETECTED <span>{appEntries.length} types</span></div>
                {appEntries.length ? (
                  <ul className="app-list">
                    {appEntries.slice(0, 7).map(([name, count]) => (
                      <li key={name}><span className="app-indicator" />{applications[name] || name}<b>{formatNumber(count)}</b></li>
                    ))}
                  </ul>
                ) : <p className="empty-report">No classified application flows.</p>}
              </div>
            </div>
            {(domainEntries.length > 0 || ruleEntries.length > 0) && (
              <div className="report-extras">
                {domainEntries.length > 0 && (
                  <div className="detail-card">
                    <div className="detail-title">TOP DOMAINS <span>{domainEntries.length} shown</span></div>
                    <ul className="app-list">
                      {domainEntries.map(([domain, count]) => (
                        <li key={domain}><span className="app-indicator" />{domain}<b>{formatNumber(count)}</b></li>
                      ))}
                    </ul>
                  </div>
                )}
                {ruleEntries.length > 0 && (
                  <div className="detail-card">
                    <div className="detail-title">RULES CONFIGURED</div>
                    <ul className="rule-summary">
                      {ruleEntries.map(([kind, count]) => (
                        <li key={kind}><span>{ruleLabels[kind] || kind}</span><b>{formatNumber(count)}</b></li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
            <div className="report-footer">
              <span>{report.sniExtractions} SNI extractions</span>
              <span>{report.classificationHits} classification hits</span>
              <span>{report.connectionsSeen} connections seen</span>
            </div>
          </section>
        )}
      </main>
      <footer className="footer">
        <span>PACKET INSPECTOR <b>·</b> LOCAL PCAP ANALYSIS</span>
        <span>Processing stays on this machine</span>
      </footer>
    </div>
  )
}

function formatNumber(value = 0) {
  return Number(value).toLocaleString()
}

function formatBytes(bytes = 0) {
  if (bytes < 1024) return `${bytes} B`
  const units = ['KB', 'MB', 'GB']
  let size = bytes / 1024
  let unit = units[0]
  for (let index = 1; size >= 1024 && index < units.length; index += 1) {
    size /= 1024
    unit = units[index]
  }
  return `${size.toFixed(size < 10 ? 1 : 0)} ${unit}`
}

function UploadIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 16V4m0 0L7 9m5-5 5 5M4 15v4a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-4" /></svg>
}

function CheckIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m5 12 4 4L19 6" /></svg>
}

function LockIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="10" width="14" height="11" rx="2" /><path d="M8 10V7a4 4 0 0 1 8 0v3m-4 5v2" /></svg>
}

function InfoIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9" /><path d="M12 11v5m0-8h.01" /></svg>
}

function ArrowIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6" /></svg>
}

function DownloadIcon() {
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3v12m0 0 5-5m-5 5-5-5M4 17v3h16v-3" /></svg>
}

export default InspectorPage
