import { useEffect, useState } from 'react'
import { processCapture } from '../api/client.js'

export function useCaptureProcessing() {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [report, setReport] = useState(null)
  const [downloadUrl, setDownloadUrl] = useState('')

  function reset() {
    setError('')
    setReport(null)
    if (downloadUrl) URL.revokeObjectURL(downloadUrl)
    setDownloadUrl('')
  }

  async function run(file, rules, username, password) {
    setBusy(true)
    setError('')
    setReport(null)
    if (downloadUrl) URL.revokeObjectURL(downloadUrl)
    setDownloadUrl('')
    try {
      const result = await processCapture(file, rules, username, password)
      setReport(result.report)
      setDownloadUrl(URL.createObjectURL(result.capture))
    } catch (requestError) {
      setError(requestError.message || 'PCAP processing failed.')
    } finally {
      setBusy(false)
    }
  }

  useEffect(() => () => {
    if (downloadUrl) URL.revokeObjectURL(downloadUrl)
  }, [downloadUrl])

  return { busy, error, report, downloadUrl, run, reset, setError }
}
