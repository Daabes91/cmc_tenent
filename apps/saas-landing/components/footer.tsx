import Link from "next/link"
import { Twitter, Facebook, Instagram, Linkedin, Github } from "lucide-react"
import {SAAS_ADMIN_URL, PATIENT_WEB_URL, SALES_EMAIL, API_DOCS_URL} from '@/lib/constants';

export default function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white py-12 text-slate-700 dark:border-gray-800 dark:bg-gray-950 dark:text-gray-400 transition-colors">
      <div className="container px-4 md:px-8">
        <div className="grid gap-8 md:grid-cols-4">
          <div>
            <Link href="/" className="flex items-center gap-2 font-bold text-xl">
              <div className="flex h-8 w-8 items-center justify-center rounded-md bg-mintlify-gradient text-white">C</div>
              <span className="bg-gradient-to-r from-primary to-mintlify-blue bg-clip-text text-transparent">
                CMC Platform
              </span>
            </Link>
            <p className="mt-4 text-slate-600 dark:text-gray-400">
              A full-stack clinic platform that gives every practice its own website, admin panel, and modern patient experience.
            </p>
            <div className="mt-6 flex space-x-4">
              <Link href="https://twitter.com" className="text-slate-500 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white" target="_blank" rel="noreferrer">
                <Twitter className="h-5 w-5" />
                <span className="sr-only">Twitter</span>
              </Link>
              <Link href="https://facebook.com" className="text-slate-500 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white" target="_blank" rel="noreferrer">
                <Facebook className="h-5 w-5" />
                <span className="sr-only">Facebook</span>
              </Link>
              <Link href="https://instagram.com" className="text-slate-500 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white" target="_blank" rel="noreferrer">
                <Instagram className="h-5 w-5" />
                <span className="sr-only">Instagram</span>
              </Link>
              <Link href="https://linkedin.com" className="text-slate-500 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white" target="_blank" rel="noreferrer">
                <Linkedin className="h-5 w-5" />
                <span className="sr-only">LinkedIn</span>
              </Link>
              <Link
                href="https://github.com/MohamedDjoudir"
                className="text-slate-500 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white"
                target="_blank"
                rel="noreferrer"
              >
                <Github className="h-5 w-5" />
                <span className="sr-only">GitHub</span>
              </Link>
            </div>
          </div>
          <div>
            <h3 className="mb-4 text-sm font-bold uppercase text-slate-500 dark:text-gray-300">Product</h3>
            <ul className="flex flex-col gap-2">
              <li>
                <Link href="#features" className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white">
                  Features
                </Link>
              </li>
              <li>
                <Link href="#how-it-works" className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white">
                  How it works
                </Link>
              </li>
              <li>
                <Link href={`${PATIENT_WEB_URL}?tenant=demo`} className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white" target="_blank" rel="noreferrer">
                  Patient demo
                </Link>
              </li>
              <li>
                <Link href={SAAS_ADMIN_URL} className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white" target="_blank" rel="noreferrer">
                  SAAS admin
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="mb-4 text-sm font-bold uppercase text-slate-500 dark:text-gray-300">Resources</h3>
            <ul className="flex flex-col gap-2">
              <li>
                <Link
                  href="/signup"
                  className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white"
                >
                  Quickstart
                </Link>
              </li>
              <li>
                <Link href={API_DOCS_URL} className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white" target="_blank" rel="noreferrer">
                  API docs
                </Link>
              </li>
              <li>
                <Link href="/blog" className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white">
                  Blog
                </Link>
              </li>
              <li>
                <Link href={`mailto:${SALES_EMAIL}`} className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white">
                  Contact
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="mb-4 text-sm font-bold uppercase text-slate-500 dark:text-gray-300">Legal</h3>
            <ul className="flex flex-col gap-2">
              <li>
                <Link href="/privacy" className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link href="/terms" className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white">
                  Terms of Service
                </Link>
              </li>
              <li>
                <Link href="/cookies" className="text-slate-600 hover:text-slate-900 dark:text-gray-400 dark:hover:text-white">
                  Cookie Policy
                </Link>
              </li>
            </ul>
          </div>
        </div>
        <div className="mt-12 border-t border-slate-200 pt-8 text-center text-slate-500 dark:border-gray-800 dark:text-gray-400">
          <p>© {new Date().getFullYear()} CMC Platform. All rights reserved.</p>
        </div>
      </div>
    </footer>
  )
}
